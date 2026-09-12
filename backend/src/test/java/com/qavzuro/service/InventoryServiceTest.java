package com.qavzuro.service;

import com.qavzuro.domain.Product;
import com.qavzuro.exception.InsufficientStockException;
import com.qavzuro.repository.InventoryHistoryRepository;
import com.qavzuro.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Covers atomic stock reservation - the mechanism that prevents overselling under concurrent checkouts. */
class InventoryServiceTest {

    private ProductRepository productRepository;
    private InventoryHistoryRepository inventoryHistoryRepository;
    private MongoTemplate mongoTemplate;
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        inventoryHistoryRepository = mock(InventoryHistoryRepository.class);
        mongoTemplate = mock(MongoTemplate.class);
        inventoryService = new InventoryService(productRepository, inventoryHistoryRepository, mongoTemplate);
    }

    @Test
    void reserve_succeedsWhenStockAvailable_andRecordsHistory() {
        Product product = Product.builder().id("p1").name("Widget").inventoryQuantity(10).reservedQuantity(0).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));

        var updateResult = mock(com.mongodb.client.result.UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(1L);
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Product.class))).thenReturn(updateResult);

        assertDoesNotThrow(() -> inventoryService.reserve("p1", null, 3, "ORDER_PLACED", "ORD-1", "user-1"));
        verify(inventoryHistoryRepository).save(any());
    }

    @Test
    void reserve_throwsWhenRequestedQuantityExceedsAvailable() {
        Product product = Product.builder().id("p1").name("Widget").inventoryQuantity(2).reservedQuantity(0).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.reserve("p1", null, 5, "ORDER_PLACED", "ORD-1", "user-1"));
    }

    @Test
    void reserve_throwsOnConcurrentConflict_evenWhenInitialCheckPassed() {
        // Simulates a race: the pre-check sees enough stock, but the atomic conditional
        // update fails because another concurrent request consumed it first.
        Product product = Product.builder().id("p1").name("Widget").inventoryQuantity(10).reservedQuantity(0).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));

        var updateResult = mock(com.mongodb.client.result.UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(0L); // conditional update matched nothing
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Product.class))).thenReturn(updateResult);

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.reserve("p1", null, 3, "ORDER_PLACED", "ORD-1", "user-1"));
    }

    @Test
    void manualAdjust_rejectsNegativeResultingInventory() {
        Product product = Product.builder().id("p1").name("Widget").inventoryQuantity(2).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class,
                () -> inventoryService.manualAdjust("staff-1", "p1", null, -5, "MANUAL_ADJUSTMENT"));
    }

    @Test
    void manualAdjust_allowsPositiveRestock() {
        Product product = Product.builder().id("p1").name("Widget").inventoryQuantity(2).build();
        when(productRepository.findById("p1")).thenReturn(Optional.of(product));
        when(productRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.manualAdjust("staff-1", "p1", null, 10, "RETURN_RESTOCK");

        assertEquals(12, product.getInventoryQuantity());
    }
}
