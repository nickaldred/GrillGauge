package com.grillgauge.api.domain.entitys;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a Hub in the system.
 * 
 * Supports PKI-based device identity:
 * - Hub submits CSR
 * - Server signs certificate
 * - Hub authenticates with mTLS
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

    /** Owner is null until pairing is completed */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", nullable = true)
    private User owner;

    /** Human-readable name (optional) */
    @Column(nullable = true)
    private String name;

    // -------------------------------
    // Pairing / OTP fields
    // -------------------------------

    /** Temporary pairing code shown on the hub */
    @Column(nullable = true)
    private String pairingCode;

    /** Optional hashed version for security (recommended) */
    @Column(nullable = true)
    private String pairingCodeHash;

    @Column(nullable = true)
    private Instant pairingCodeExpiresAt;

    // -------------------------------
    // PKI fields
    // -------------------------------

    /** CSR submitted by the hub */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String csrPem;

    /** Extracted public key from CSR */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String publicKeyPem;

    /** Signed X.509 certificate returned to the hub */
    @Column(nullable = true, columnDefinition = "TEXT")
    private String certificatePem;

    /** Certificate serial number (unique per certificate) */
    @Column(nullable = true)
    private Long certificateSerial;

    /** Certificate issued + expiration timestamps */
    @Column(nullable = true)
    private Instant certificateIssuedAt;

    @Column(nullable = true)
    private Instant certificateExpiresAt;

    /** Indicates whether hub is fully paired + certificate issued */
    @Column(nullable = false)
    private boolean paired = false;

    // -------------------------------
    // General hub info
    // -------------------------------

    @OneToMany(mappedBy = "hub", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Probe> probes = new ArrayList<>();

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    private Instant lastSeenAt;

    /** Optional: revocation info */
    @Column(nullable = true)
    private Instant revokedAt;

    @Column(nullable = true)
    private String revocationReason;

    // Convenience constructor
    public Hub(final String name) {
        this.name = name;
    }

    public Hub(final User owner, final String name) {
        this.owner = owner;
        this.name = name;
    }
}
