package com.qavzuro.repository;

import com.qavzuro.domain.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends MongoRepository<Role, String> {
    Optional<Role> findByCode(String code);
    List<Role> findByCodeIn(List<String> codes);
    boolean existsByCode(String code);
}
