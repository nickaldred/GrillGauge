package com.grillgauge.api.services;

import org.springframework.stereotype.Service;

import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.ReadingRepository;

@Service
public class HubService {
    private HubRepository hubRepository;
    private ReadingRepository readingRepository;

    public HubService(final HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    public HubReading saveHubReading(final HubReading hubReading) {
        for (ProbeReading probeReading : hubReading.getProbes()) {
            saveProbeReading(probeReading);
        }
        return hubReading;
    }

    public Reading saveProbeReading(final ProbeReading probeReading, final Long hubId) {
        final Reading reading = new Reading(null, null, null);
        List<Probe>
        // Get the actual Probe database ID
        readingRepository.save(new Reading(null, null, null, null));
    }
}
