package com.grillgauge.api.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProbeReading {
    private Integer id;
    private Float currentTemp;
}
