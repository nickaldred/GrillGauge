package com.grillgauge.api.domain.entitys;

import java.time.Instant;

import org.springframework.lang.NonNull;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NonNull
    private Long probeId;
    @NonNull
    private Instant timeStamp;
    @NonNull
    private Float currentTemp;

    public Reading(final Long probeId, final Instant timeStamp, final Float currentTemp) {
        this.probeId = probeId;
        this.timeStamp = timeStamp;
        this.currentTemp = currentTemp;
    }
}
