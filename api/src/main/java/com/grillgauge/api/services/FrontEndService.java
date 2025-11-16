package com.grillgauge.api.services;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.domain.models.FrontEndProbe;

@Service
public class FrontEndService {
    private static final Logger LOG = LoggerFactory.getLogger(FrontEndService.class);

    private HubService hubService;
    private ProbeService probeService;

    public FrontEndService(HubService hubService, ProbeService probeService) {
        this.hubService = hubService;
        this.probeService = probeService;
    }

    private String getProbeColour(Float targetTemp, Float currentTemp) {
        if (currentTemp == null) {
            return "gray"; // No reading
        }

        if (targetTemp == null) {
            return "blue"; // No target set, just showing current
        }

        float difference = currentTemp - targetTemp;

        if (difference >= 10.0f) {
            return "red";
        } else if (difference >= 5.0f) {
            return "darkgreen";
        } else if (difference >= 0f) {
            return "green";
        } else if (difference >= -10.0f) {
            return "yellow";
        } else if (difference >= -20.0f) {
            return "orange";
        } else {
            return "lightblue";
        }
    }

    /**
     * Get all hubs and their probes for the given user email.
     * 
     * @param email user email
     * @return List of FrontEndHub models.
     */
    public List<FrontEndHub> getHubs(final String email) {
        LOG.info("Getting hubs for user ID: {}", email);
        List<Hub> hubs = hubService.getHubsByEmail(email);

        List<FrontEndHub> dashboardHubs = hubs.stream()
                .map(hub -> {
                    List<FrontEndProbe> probes = probeService.getProbesByHubId(hub.getId()).stream()
                            .map(probe -> {
                                Float currentTemp = probeService.getCurrentTemperature(probe.getId());
                                return new FrontEndProbe(
                                        probe.getId(),
                                        probe.getLocalId(),
                                        probe.getTargetTemp(),
                                        currentTemp,
                                        probe.getName(),
                                        getProbeColour(probe.getTargetTemp(), currentTemp),
                                        (currentTemp == null || currentTemp.isNaN() ? false : true));
                            })
                            .toList();

                    final boolean connected = probes.stream().anyMatch(p -> p.getConnected());
                    return new FrontEndHub(hub.getId(), hub.getName(), probes, connected);
                })
                .toList();

        LOG.info("Successfully got {} hubs for user ID: {}", dashboardHubs.size(), email);
        return dashboardHubs;
    }
}
