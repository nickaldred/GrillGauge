package com.grillgauge.api.domain.models;

import java.util.List;
import java.util.Map;

import com.grillgauge.api.domain.entitys.Probe;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Dashboard {
    private Long userId;
    private List<DashboardHub> hubs;
}
