package com.qavzuro.service;

import com.qavzuro.domain.InventoryHistoryEntry;
import com.qavzuro.domain.Product;
import com.qavzuro.domain.ProductVariant;
import com.qavzuro.exception.InsufficientStockException;
import com.qavzuro.exception.ResourceNotFoundException;
import com.qavzuro.repository.InventoryHistoryRepository;
import com.qavzuro.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final MongoTemplate mongoTemplate;

    public int availableQuantity(Product product, String variantId) {
        if (variantId == null || variantId.isBlank()) {
            return product.getAvailableQuantity();
        }
        return product.getVariants().stream()
                .filter(v -> v.getVariantId().equals(variantId))
                .findFirst()
                .map(v -> Math.max(0, v.getStockQuantity() - v.getReservedQuantity()))
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found."));
    }

    /**
     * Atomically reserves stock using a MongoDB conditional update so concurrent
     * checkouts cannot both succeed against the same last unit (avoids overselling).
     */
    public void reserve(String productId, String variantId, int quantity, String reason, String reference, String actorUserId) {
        if (variantId == null || variantId.isBlank()) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
            if (product.getAvailableQuantity() < quantity) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            Update update = new Update().inc("reservedQuantity", quantity);
            Query condition = new Query(Criteria.where("_id").is(productId)
                    .and("reservedQuantity").lte(product.getInventoryQuantity() - quantity));
            var result = mongoTemplate.updateFirst(condition, update, Product.class);
            if (result.getModifiedCount() == 0) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            recordHistory(productId, null, -quantity, product.getInventoryQuantity() - product.getReservedQuantity() - quantity,
                    reason, reference, actorUserId);
        } else {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getVariantId().equals(variantId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Product variant not found."));
            int available = variant.getStockQuantity() - variant.getReservedQuantity();
            if (available < quantity) {
                throw new InsufficientStockException("Insufficient stock for variant of: " + product.getName());
            }
            variant.setReservedQuantity(variant.getReservedQuantity() + quantity);
            productRepository.save(product);
            recordHistory(productId, variantId, -quantity, available - quantity, reason, reference, actorUserId);
        }
    }

    /** Releases a previously reserved quantity without reducing on-hand stock (e.g. cancelled order). */
    public void release(String productId, String variantId, int quantity, String reason, String reference, String actorUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (variantId == null || variantId.isBlank()) {
            product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - quantity));
            productRepository.save(product);
        } else {
            product.getVariants().stream()
                    .filter(v -> v.getVariantId().equals(variantId))
                    .findFirst()
                    .ifPresent(v -> v.setReservedQuantity(Math.max(0, v.getReservedQuantity() - quantity)));
            productRepository.save(product);
        }
        recordHistory(productId, variantId, quantity, -1, reason, reference, actorUserId);
    }

    /** Confirms a reservation as a real deduction (e.g. on payment success): reduces both on-hand and reserved. */
    public void commitReservation(String productId, String variantId, int quantity, String reason, String reference, String actorUserId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        if (variantId == null || variantId.isBlank()) {
            product.setInventoryQuantity(Math.max(0, product.getInventoryQuantity() - quantity));
            product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - quantity));
        } else {
            product.getVariants().stream()
                    .filter(v -> v.getVariantId().equals(variantId))
                    .findFirst()
                    .ifPresent(v -> {
                        v.setStockQuantity(Math.max(0, v.getStockQuantity() - quantity));
                        v.setReservedQuantity(Math.max(0, v.getReservedQuantity() - quantity));
                    });
        }
        productRepository.save(product);
        recordHistory(productId, variantId, -quantity, product.getInventoryQuantity(), reason, reference, actorUserId);
    }

    public void manualAdjust(String actorUserId, String productId, String variantId, int change, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));
        int after;
        if (variantId == null || variantId.isBlank()) {
            int newQty = product.getInventoryQuantity() + change;
            if (newQty < 0) throw new InsufficientStockException("Adjustment would result in negative inventory.");
            product.setInventoryQuantity(newQty);
            after = newQty;
        } else {
            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getVariantId().equals(variantId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found."));
            int newQty = variant.getStockQuantity() + change;
            if (newQty < 0) throw new InsufficientStockException("Adjustment would result in negative inventory.");
            variant.setStockQuantity(newQty);
            after = newQty;
        }
        productRepository.save(product);
        recordHistory(productId, variantId, change, after, reason, null, actorUserId);
    }

    public Page<InventoryHistoryEntry> history(String productId, Pageable pageable) {
        return inventoryHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    private void recordHistory(String productId, String variantId, int change, int after, String reason, String reference, String actorUserId) {
        inventoryHistoryRepository.save(InventoryHistoryEntry.builder()
                .productId(productId)
                .variantId(variantId)
                .quantityChange(change)
                .quantityAfter(after)
                .reason(reason)
                .reference(reference)
                .actorUserId(actorUserId)
                .build());
    }
}
