package com.grillgauge.api.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.ProbeRepository;

@Service
public class ProbeService {
    private ReadingService readingService;
    private ProbeRepository probeRepository;

    public ProbeService(final ProbeRepository probeRepository, final ReadingService readingService) {
        this.probeRepository = probeRepository;
        this.readingService = readingService;
    }

    public List<Probe> getProbes(final Long hubId) {
        List<Probe> probes = probeRepository.findByHubId(hubId);
        if (probes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No probes found for hub ID: %s".formatted(hubId));
        }
        return probes;
    }

    public Reading saveProbeReading(final ProbeReading probeReading, final Long hubId) {
        List<Probe> probes = getProbes(hubId);
        Probe probe = probes.stream()
                .filter(x -> x.getLocalId().equals(probeReading.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Probe with ID: %s and HubId: %s not found".formatted(probeReading.getId(), hubId)));
        return readingService.saveCurrentReading(probe.getId(), probeReading.getCurrentTemp());
    }
}
