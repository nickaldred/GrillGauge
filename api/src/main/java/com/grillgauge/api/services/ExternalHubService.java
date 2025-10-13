package com.grillgauge.api.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.domain.models.ProbeReading;

/**
 * Service class for managing hubs and their readings.
 */
@Service
public class ExternalHubService {

    private final HubService hubService;

    private final ProbeService probeService;

    public ExternalHubService(final HubService hubService, final ProbeService probeService) {
        this.hubService = hubService;
        this.probeService = probeService;

    }

    /**
     * Save the hub reading for the given hubId.
     * 
     * @param hubReading the HubReading containing the probe readings
     * @param hubId      the hubId to which the hub belongs
     * @return the saved HubReading entity
     */
    @Transactional
    public HubReading saveHubReading(final HubReading hubReading, final Long hubId) {
        for (ProbeReading probeReading : hubReading.getProbeReadings()) {
            probeService.saveProbeReading(probeReading, hubId);
        }
        return hubReading;
    }

    /**
     * Get the current state of the hub for the given hubId, including its probes.
     * 
     * @param hubId hubId to get the current state for
     * @return HubCurrentState containing the hubId, hub name, and list of probes
     */
    public HubCurrentState getHubCurrentState(final Long hubId) {
        Hub hub = hubService.getHub(hubId);
        List<Probe> probes = probeService.getProbesByHubId(hubId);
        return new HubCurrentState(hubId, hub.getName(), probes);
    }
}
