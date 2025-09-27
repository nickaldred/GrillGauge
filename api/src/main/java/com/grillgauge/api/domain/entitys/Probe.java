package com.grillgauge.api.domain.entitys;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

public class Probe {

    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NonNull
    private Integer localId;
    @NonNull
    private Long hubId;
    @Nullable
    private String targetTemp;
    @Nullable
    private String name;
}
