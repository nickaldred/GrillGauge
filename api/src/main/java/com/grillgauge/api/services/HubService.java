package com.grillgauge.api.services;

import org.springframework.stereotype.Service;

import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.HubRepository;

@Service
public class HubService {

    private final ProbeService probeService;
    private HubRepository hubRepository;

    public HubService(final HubRepository hubRepository, final ProbeService probeService) {
        this.hubRepository = hubRepository;
        this.probeService = probeService;

    }

    public HubReading saveHubReading(final HubReading hubReading) {
        for (ProbeReading probeReading : hubReading.getProbeReadings()) {
            probeService.saveProbeReading(probeReading, hubReading.getId());
        }
        return hubReading;
    }
}
