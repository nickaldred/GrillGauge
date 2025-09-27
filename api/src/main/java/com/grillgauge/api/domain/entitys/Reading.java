package com.grillgauge.api.domain.entitys;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NonNull
    private Long probeId;
    @NonNull
    private LocalDateTime timeStamp;
    @NonNull
    private Float currentTemp;

    public Reading(final Long probeId, final LocalDateTime timeStamp, final Float currentTemp) {
        this.probeId = probeId;
        this.timeStamp = timeStamp;
        this.currentTemp = currentTemp;
    }
}
