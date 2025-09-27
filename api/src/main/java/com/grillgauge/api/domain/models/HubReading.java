package com.grillgauge.api.domain.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HubReading {
    private Long id;
    private List<ProbeReading> probeReadings;
}
