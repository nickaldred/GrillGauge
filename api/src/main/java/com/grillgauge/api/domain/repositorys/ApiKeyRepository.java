package com.grillgauge.api.domain.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import com.grillgauge.api.domain.entitys.ApiKey;
import java.util.Optional;

/**
 * Repository interface for managing ApiKey entities.
 */
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyHashAndHubIdAndActiveTrue(String keyHash, Long hubId);
}
