package com.qavzuro.repository;

import com.qavzuro.domain.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReturnRequestRepository extends MongoRepository<ReturnRequest, String> {
    Page<ReturnRequest> findByCustomerId(String customerId, Pageable pageable);
    Page<ReturnRequest> findByOrderId(String orderId, Pageable pageable);
}
