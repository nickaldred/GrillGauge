package com.grillgauge.api.domain.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HubReading {
    private List<ProbeReading> probes;
}
