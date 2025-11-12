package com.grillgauge.api.domain.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Dashboard {
    private String email;
    private List<DashboardHub> hubs;
}
