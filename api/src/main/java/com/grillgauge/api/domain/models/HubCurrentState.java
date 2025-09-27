package com.grillgauge.api.domain.models;

import java.util.List;

import com.grillgauge.api.domain.entitys.Probe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HubCurrentState {
    private Long hubId;
    private String hubName;
    private List<Probe> probes;
}
