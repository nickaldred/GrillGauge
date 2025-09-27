package com.grillgauge.api.domain.entitys;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    @NonNull
    private Integer localId;
    @NonNull
    private Long hubId;
    @Nullable
    private Float targetTemp;
    @Nullable
    private String name;

    public Probe(Integer localId, Long hubId) {
        this(localId, hubId, null, null);
    }

    public Probe(Integer localId, Long hubId, Float targetTemp) {
        this(localId, hubId, targetTemp, null);
    }

    public Probe(Integer localId, Long hubId, Float targetTemp, String name) {
        this.localId = localId;
        this.hubId = hubId;
        this.targetTemp = targetTemp;
        this.name = name;
    }
}
