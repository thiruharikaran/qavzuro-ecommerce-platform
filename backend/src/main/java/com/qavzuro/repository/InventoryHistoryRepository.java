package com.qavzuro.repository;

import com.qavzuro.domain.InventoryHistoryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InventoryHistoryRepository extends MongoRepository<InventoryHistoryEntry, String> {
    Page<InventoryHistoryEntry> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);
}
