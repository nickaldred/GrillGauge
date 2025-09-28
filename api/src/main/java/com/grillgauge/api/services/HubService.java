package com.grillgauge.api.services;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.HubRepository;

/**
 * Service class for managing hubs and their readings.
 */
@Service
public class HubService {

    private final ProbeService probeService;
    private HubRepository hubRepository;

    public HubService(final HubRepository hubRepository, final ProbeService probeService) {
        this.hubRepository = hubRepository;
        this.probeService = probeService;

    }

    /**
     * Get the hub for the given hubId.
     * 
     * @param hubId hubId to get the hub for
     * @return Hub entity
     * @throws ResponseStatusException with status 404 if no hub is found for the
     *                                 given hubId
     */
    public Hub getHub(final Long hubId) {
        Optional<Hub> hub = hubRepository.findById(hubId);
        if (hub.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No hub found for hub ID: %s".formatted(hubId));
        }
        return hub.get();
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
        Hub hub = getHub(hubId);
        List<Probe> probes = probeService.getProbes(hubId);
        return new HubCurrentState(hubId, hub.getName(), probes);
    }

    /**
     * Store a new hub.
     * 
     * @param hub the Hub entity to store
     * @return the stored Hub entity
     */
    public void deleteHub(final Long hubId) {
        hubRepository.deleteById(hubId);
    }
}
