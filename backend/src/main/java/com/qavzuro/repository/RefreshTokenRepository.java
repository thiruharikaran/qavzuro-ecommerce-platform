package com.qavzuro.repository;

import com.qavzuro.domain.RefreshToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findByFamilyId(String familyId);
    List<RefreshToken> findByUserIdAndRevokedFalse(String userId);
}
