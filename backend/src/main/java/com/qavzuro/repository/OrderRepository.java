package com.qavzuro.repository;

import com.qavzuro.domain.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> findByOrderNumber(String orderNumber);
    Page<Order> findByCustomerId(String customerId, Pageable pageable);
    boolean existsByOrderNumber(String orderNumber);
}
