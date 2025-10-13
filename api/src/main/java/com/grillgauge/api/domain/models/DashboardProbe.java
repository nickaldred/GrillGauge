package com.grillgauge.api.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DashboardProbe {

    private Long id;
    private Integer localId;
    private Float targetTemp;
    private Float currentTemp;
    private String name;
}
