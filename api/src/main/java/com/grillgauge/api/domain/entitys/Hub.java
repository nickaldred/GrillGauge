package com.grillgauge.api.domain.entitys;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.grillgauge.api.domain.enums.HubStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Entity representing a Hub in the system.
 *
 * Supports:
 * - unauthenticated registration
 * - OTP-based pairing
 * - PKI-based device identity (CSR → signed certificate)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Hub {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Owner is null until pairing completes.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", nullable = true)
    private User owner;

    // -------------------------------
    // Registration / OTP
    // -------------------------------

    /**
     * Raw pairing code (ONLY stored unhashed if you're debugging).
     * Recommended: null this after hashing.
     */
    @Column(nullable = true)
    private String pairingCode;

    /**
     * Secure hashed OTP (preferred).
     */
    @Column(nullable = true)
    private String pairingCodeHash;

    @Column(nullable = true)
    private Instant pairingCodeExpiresAt;

    @Column(nullable = false)
    private HubStatus status = HubStatus.PENDING;

    // -------------------------------
    // PKI fields
    // -------------------------------

    /** CSR submitted by hub */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String csrPem;

    /** Extracted public key */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String publicKeyPem;

    /** Signed X.509 cert returned to hub after pairing */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String certificatePem;

    /** Unique certificate serial number */
    @Column(nullable = true)
    private Long certificateSerial;

    /** Certificate validity */
    @Column(nullable = true)
    private Instant certificateIssuedAt;

    @Column(nullable = true)
    private Instant certificateExpiresAt;

    /** True = hub has completed pairing + certificate issuance */
    @Column(nullable = false)
    private boolean paired = false;

    // -------------------------------
    // Generic Hub Info
    // -------------------------------

    @Column(nullable = true)
    private String name;

    @OneToMany(mappedBy = "hub", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Probe> probes = new ArrayList<>();

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant lastSeenAt;

    @JdbcTypeCode(SqlTypes.JSON)
    // Use a text column for H2 (in-memory/test). PostgreSQL can use `jsonb` in
    // production.
    @Column(columnDefinition = "text")
    private Map<String, String> metadata;

    // -------------------------------
    // Revocation (if certificate revoked)
    // -------------------------------

    @Column(nullable = true)
    private Instant revokedAt;

    @Column(nullable = true)
    private String revocationReason;

    // Convenience constructors

    public Hub(final String name) {
        this.name = name;
    }

    public Hub(final User owner, final String name) {
        this.owner = owner;
        this.name = name;
    }

    public Hub(final String otp, final Instant otpExpiresAt, final Map<String, String> metaData) {
        this.pairingCode = otp;
        this.pairingCodeExpiresAt = otpExpiresAt;
        this.metadata = metaData;
    }
}
