package com.qavzuro.repository;

import com.qavzuro.domain.Permission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PermissionRepository extends MongoRepository<Permission, String> {
    Optional<Permission> findByCode(String code);
    boolean existsByCode(String code);
}
