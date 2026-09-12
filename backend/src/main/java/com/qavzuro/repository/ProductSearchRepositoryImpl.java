package com.qavzuro.repository;

import com.qavzuro.domain.Product;
import com.qavzuro.domain.ProductStatus;
import com.qavzuro.dto.request.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Repository
@RequiredArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Product> search(ProductSearchRequest c, Pageable pageable) {
        List<Criteria> criteriaList = new ArrayList<>();

        // Only ACTIVE products are visible in public search unless explicitly overridden.
        criteriaList.add(Criteria.where("status").is(c.getStatus() != null ? c.getStatus() : ProductStatus.ACTIVE));

        if (c.getKeyword() != null && !c.getKeyword().isBlank()) {
            Pattern pattern = Pattern.compile(Pattern.quote(c.getKeyword()), Pattern.CASE_INSENSITIVE);
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("name").regex(pattern),
                    Criteria.where("description").regex(pattern),
                    Criteria.where("brand").regex(pattern),
                    Criteria.where("tags").regex(pattern)
            ));
        }
        if (c.getCategoryId() != null) {
            criteriaList.add(Criteria.where("categoryId").is(c.getCategoryId()));
        }
        if (c.getBrand() != null) {
            criteriaList.add(Criteria.where("brand").is(c.getBrand()));
        }
        if (c.getMinPrice() != null) {
            criteriaList.add(Criteria.where("price").gte(c.getMinPrice()));
        }
        if (c.getMaxPrice() != null) {
            criteriaList.add(Criteria.where("price").lte(c.getMaxPrice()));
        }
        if (c.getMinRating() != null) {
            criteriaList.add(Criteria.where("ratingSummary.average").gte(c.getMinRating()));
        }
        if (Boolean.TRUE.equals(c.getInStockOnly())) {
            criteriaList.add(Criteria.where("inventoryQuantity").gt(0));
        }

        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, Product.class);

        query.with(pageable);
        List<Product> results = mongoTemplate.find(query, Product.class);

        return new PageImpl<>(results, pageable, total);
    }
}
