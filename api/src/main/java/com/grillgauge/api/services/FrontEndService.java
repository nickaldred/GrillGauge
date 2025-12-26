package com.grillgauge.api.services;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.domain.models.FrontEndProbe;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service class for managing front-end related operations.
 *
 * <p>Provides methods for retrieving hubs and their associated probes for a user.
 */
@Service
public class FrontEndService {
  private static final Logger LOG = LoggerFactory.getLogger(FrontEndService.class);

  private HubService hubService;
  private ProbeService probeService;

  public FrontEndService(HubService hubService, ProbeService probeService) {
    this.hubService = hubService;
    this.probeService = probeService;
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

    List<FrontEndHub> dashboardHubs =
        hubs.stream()
            .map(
                hub -> {
                  List<FrontEndProbe> probes =
                      probeService.getProbesByHubId(hub.getId()).stream()
                          .map(
                              probe -> {
                                Float currentTemp =
                                    probeService.getCurrentTemperature(probe.getId());
                                return new FrontEndProbe(
                                    probe.getId(),
                                    probe.getLocalId(),
                                    probe.getTargetTemp(),
                                    currentTemp,
                                    probe.getName(),
                                    probe.getColour(),
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
