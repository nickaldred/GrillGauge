package com.grillgauge.api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.models.HubCurrentState;
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

    public Hub getHub(final Long hubId) {
        Optional<Hub> hub = hubRepository.findById(hubId);
        if (hub.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No hub found for hub ID: %s".formatted(hubId));
        }
        return hub.get();
    }

    public HubReading saveHubReading(final HubReading hubReading, final Long hubId) {
        for (ProbeReading probeReading : hubReading.getProbeReadings()) {
            probeService.saveProbeReading(probeReading, hubId);
        }
        return hubReading;
    }

    public HubCurrentState getHubCurrentState(final Long hubId) { // TODO - Replace with API key
        Hub hub = getHub(hubId);
        List<Probe> probes = probeService.getProbes(hubId);
        return new HubCurrentState(hubId, hub.getName(), probes);
    }
}
