package com.grillgauge.api.domain.entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Probe {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "local_id", nullable = false)
    private Integer localId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", nullable = false)
    private Hub hub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = true)
    private Float targetTemp;

    @Column(nullable = true)
    private String name;

    public Probe(Integer localId, Hub hub, User owner) {
        this(localId, hub, owner, null, null);
    }

    public Probe(Integer localId, Hub hub, User owner, Float targetTemp) {
        this(localId, hub, owner, targetTemp, null);
    }

    public Probe(Integer localId, Hub hub, User owner, Float targetTemp, String name) {
        this.localId = localId;
        this.hub = hub;
        this.owner = owner;
        this.targetTemp = targetTemp;
        this.name = name;
    }
}
