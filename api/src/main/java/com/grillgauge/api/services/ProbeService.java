package com.grillgauge.api.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import com.grillgauge.api.domain.repositorys.ReadingRepository;

@Service
public class ProbeService {
    private ReadingRepository readingRepository;
    private ProbeRepository probeRepository;

    public ProbeService(final ProbeRepository probeRepository, final ReadingRepository readingRepository) {
        this.probeRepository = probeRepository;
        this.readingRepository = readingRepository;
    }

    public Reading saveProbeReading(final ProbeReading probeReading, final Long hubId) {
        List<Probe> probes = probeRepository.findByHubId(hubId);
        Probe probe = probes.stream()
                .filter(x -> x.getLocalId().equals(probeReading.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Probe with ID: %s and HubId: %s not found".formatted(probeReading.getId(), hubId)));
        Reading reading = new Reading(probe.getId(), LocalDateTime.now(), probeReading.getCurrentTemp());
        readingRepository.save(reading);
        return reading;
    }
}
