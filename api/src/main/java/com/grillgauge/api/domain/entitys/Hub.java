package com.grillgauge.api.domain.entitys;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Hub {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String apiKey;

    @Column(nullable = true)
    private String name;

    private Instant createdAt = Instant.now();

    public Hub(final User owner, final String apiKey, final String name) {
        this.owner = owner;
        this.apiKey = apiKey;
        this.name = name;
    }
}
