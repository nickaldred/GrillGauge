package com.grillgauge.api.domain.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HubCurrentState {
    private String apiKey;
    private String hubName;
    private List<ProbeReading> probes;
}
