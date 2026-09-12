package com.qavzuro.service;

import com.qavzuro.domain.Product;
import com.qavzuro.domain.ProductImage;
import com.qavzuro.domain.ProductStatus;
import com.qavzuro.dto.request.CreateProductRequest;
import com.qavzuro.dto.request.ProductSearchRequest;
import com.qavzuro.dto.request.UpdateProductRequest;
import com.qavzuro.exception.ConflictException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final AuditService auditService;
    private final MongoTemplate mongoTemplate;

    public Page<Product> search(ProductSearchRequest req) {
        Pageable pageable = PageRequest.of(Math.max(req.getPage(), 0), Math.min(Math.max(req.getSize(), 1), 100), resolveSort(req.getSort()));
        return productRepository.search(req, pageable);
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "relevance" : sort) {
            case "newest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "rating" -> Sort.by(Sort.Direction.DESC, "ratingSummary.average");
            case "popularity" -> Sort.by(Sort.Direction.DESC, "popularityScore");
            default -> Sort.by(Sort.Direction.DESC, "createdAt"); // relevance falls back to recency
        };
    }

    public Product getBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }

    public Product getById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
    }

    public List<Product> relatedProducts(Product product, int limit) {
        ProductSearchRequest req = new ProductSearchRequest();
        req.setCategoryId(product.getCategoryId());
        req.setSize(limit + 1);
        Page<Product> page = search(req);
        List<Product> results = new ArrayList<>(page.getContent());
        results.removeIf(p -> p.getId().equals(product.getId()));
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    public Product create(String actorUserId, CreateProductRequest req) {
        if (productRepository.existsBySku(req.getSku())) {
            throw new ConflictException("A product with this SKU already exists.");
        }
        String slug = slugify(req.getName());
        String uniqueSlug = slug;
        int suffix = 1;
        while (productRepository.existsBySlug(uniqueSlug)) {
            uniqueSlug = slug + "-" + (++suffix);
        }

        List<ProductImage> images = new ArrayList<>();
        if (req.getImages() != null) {
            int order = 0;
            for (var img : req.getImages()) {
                images.add(ProductImage.builder()
                        .url(img.get("url"))
                        .altText(img.get("altText"))
                        .sortOrder(order)
                        .primary(order == 0)
                        .build());
                order++;
            }
        }

        Product product = Product.builder()
                .sku(req.getSku())
                .slug(uniqueSlug)
                .name(req.getName())
                .description(req.getDescription())
                .shortDescription(req.getShortDescription())
                .brand(req.getBrand())
                .categoryId(req.getCategoryId())
                .subcategoryId(req.getSubcategoryId())
                .price(req.getPrice())
                .salePrice(req.getSalePrice())
                .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                .taxRatePercent(req.getTaxRatePercent())
                .inventoryQuantity(req.getInventoryQuantity())
                .lowStockThreshold(req.getLowStockThreshold() > 0 ? req.getLowStockThreshold() : 5)
                .status(ProductStatus.DRAFT)
                .images(images)
                .specifications(req.getSpecifications())
                .attributes(req.getAttributes())
                .tags(req.getTags())
                .build();

        Product saved = productRepository.save(product);
        auditService.record(actorUserId, null, "PRODUCT_CREATED", "PRODUCT", saved.getId(), null);
        return saved;
    }

    public Product update(String actorUserId, String id, UpdateProductRequest req) {
        Product product = getById(id);
        if (req.getName() != null) product.setName(req.getName());
        if (req.getDescription() != null) product.setDescription(req.getDescription());
        if (req.getShortDescription() != null) product.setShortDescription(req.getShortDescription());
        if (req.getBrand() != null) product.setBrand(req.getBrand());
        if (req.getCategoryId() != null) product.setCategoryId(req.getCategoryId());
        if (req.getSubcategoryId() != null) product.setSubcategoryId(req.getSubcategoryId());
        if (req.getPrice() != null) product.setPrice(req.getPrice());
        if (req.getSalePrice() != null) product.setSalePrice(req.getSalePrice());
        if (req.getTaxRatePercent() != null) product.setTaxRatePercent(req.getTaxRatePercent());
        if (req.getLowStockThreshold() != null) product.setLowStockThreshold(req.getLowStockThreshold());
        if (req.getStatus() != null) product.setStatus(req.getStatus());
        if (req.getSpecifications() != null) product.setSpecifications(req.getSpecifications());
        if (req.getAttributes() != null) product.setAttributes(req.getAttributes());
        if (req.getTags() != null) product.setTags(req.getTags());

        Product saved = productRepository.save(product);
        auditService.record(actorUserId, null, "PRODUCT_UPDATED", "PRODUCT", saved.getId(), null);
        return saved;
    }

    public void delete(String actorUserId, String id) {
        Product product = getById(id);
        product.setStatus(ProductStatus.ARCHIVED); // soft delete: preserves order history integrity
        productRepository.save(product);
        auditService.record(actorUserId, null, "PRODUCT_ARCHIVED", "PRODUCT", id, null);
    }

    public List<Product> lowStock() {
        // A product is "low stock" if (inventoryQuantity - reservedQuantity) <= its own threshold.
        // Compared as a database-side $expr across two fields of the same document, combined with
        // the status filter, so this runs as one indexed-status Mongo query rather than a full
        // in-memory scan-and-filter of the catalog.
        org.bson.Document expr = new org.bson.Document("$expr",
                new org.bson.Document("$lte", java.util.List.of(
                        new org.bson.Document("$subtract", java.util.List.of("$inventoryQuantity", "$reservedQuantity")),
                        "$lowStockThreshold"
                )));

        Query query = new Query(Criteria.where("status").is(ProductStatus.ACTIVE));
        query.addCriteria(new Criteria() {
            @Override
            public org.bson.Document getCriteriaObject() {
                return expr;
            }
        });
        return mongoTemplate.find(query, Product.class);
    }

    public static String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\p{ASCII}]").matcher(normalized).replaceAll("");
        slug = slug.toLowerCase().trim().replaceAll("[^a-z0-9\\s-]", "").replaceAll("\\s+", "-");
        return slug;
    }
}
