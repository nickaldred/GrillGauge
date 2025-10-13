package com.grillgauge.api.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.models.Dashboard;
import com.grillgauge.api.domain.models.DashboardHub;
import com.grillgauge.api.domain.models.DashboardProbe;
import com.grillgauge.api.domain.models.HubCurrentState;

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

    public Dashboard getDashboard(final long userId) {

        List<Hub> hubs = hubService.getHubsByUserId(userId);

        List<DashboardHub> dashboardHubs = hubs.stream()
                .map(hub -> {
                    List<DashboardProbe> probes = probeService.getProbesByHubId(hub.getId()).stream()
                            .map(probe -> {
                                float currentTemp = probeService.getCurrentTemperature(probe.getId());
                                return new DashboardProbe(
                                        probe.getId(),
                                        probe.getLocalId(),
                                        probe.getTargetTemp(),
                                        currentTemp,
                                        probe.getName());
                            })
                            .toList();

                    return new DashboardHub(hub.getId(), hub.getName(), probes);
                })
                .toList();
        return new Dashboard(userId, dashboardHubs);
    }

}
