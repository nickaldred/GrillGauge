package com.grillgauge.api.domain.entitys;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class ApiKey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String keyHash;

    @Column(nullable = false)
    private Long hubId;

    private String label;
    private Instant createdAt = Instant.now();
    private Instant expiresAt;
    private boolean active = true;
    private Instant lastUsedAt;

    public ApiKey(final String keyHash, final Long hubId, final String label) {
        this.keyHash = keyHash;
        this.hubId = hubId;
        this.label = label;
    }
}
