package com.grillgauge.api.domain.entitys;

import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Hub {

    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NonNull
    private Long userId;
    @NonNull
    private String apiKey;
    @Nullable
    private String name;
}
