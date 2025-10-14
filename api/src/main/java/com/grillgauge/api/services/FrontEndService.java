package com.grillgauge.api.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.models.Dashboard;
import com.grillgauge.api.domain.models.DashboardHub;
import com.grillgauge.api.domain.models.DashboardProbe;

@Service
public class FrontEndService {

    private HubService hubService;
    private UserService userService;
    private ProbeService probeService;

    public FrontEndService(HubService hubService, UserService userService, ProbeService probeService) {
        this.hubService = hubService;
        this.userService = userService;
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
            return "red"; // Well above target
        } else if (difference >= 5.0f) {
            return "darkgreen"; // Well above target
        } else if (difference >= 0f) {
            return "green"; // At or slightly above target
        } else if (difference >= -10.0f) {
            return "yellow"; // Almost there (within 2°C below target)
        } else if (difference >= -20.0f) {
            return "orange"; // Moderately below target
        } else {
            return "lightblue"; // Far below target
        }
    }

    public Dashboard getDashboard(final long userId) {

        List<Hub> hubs = hubService.getHubsByUserId(userId);

        List<DashboardHub> dashboardHubs = hubs.stream()
                .map(hub -> {
                    List<DashboardProbe> probes = probeService.getProbesByHubId(hub.getId()).stream()
                            .map(probe -> {
                                Float currentTemp = probeService.getCurrentTemperature(probe.getId());
                                return new DashboardProbe(
                                        probe.getId(),
                                        probe.getLocalId(),
                                        probe.getTargetTemp(),
                                        currentTemp,
                                        probe.getName(),
                                        getProbeColour(probe.getTargetTemp(), currentTemp),
                                        (currentTemp == null || currentTemp.isNaN() ? false : true));
                            })
                            .toList();

                    return new DashboardHub(hub.getId(), hub.getName(), probes);
                })
                .toList();
        return new Dashboard(userId, dashboardHubs);
    }

}
