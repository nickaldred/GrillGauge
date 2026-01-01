package com.grillgauge.api.services;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.domain.models.FrontEndProbe;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Service class for managing front-end related operations. */
@Service
public class FrontEndService {
  private static final Logger LOG = LoggerFactory.getLogger(FrontEndService.class);

  private final HubService hubService;
  private final ProbeService probeService;
  private final DemoService demoService;
  private final TemperatureConversionService temperatureConversionService;

  public FrontEndService(
      final HubService hubService,
      final ProbeService probeService,
      final DemoService demoService,
      final TemperatureConversionService temperatureConversionService) {
    this.hubService = hubService;
    this.probeService = probeService;
    this.demoService = demoService;
    this.temperatureConversionService = temperatureConversionService;
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
    if (hubs.isEmpty()) {
      LOG.info("No hubs found for user ID: {}", email);
      return List.of();
    }

    List<FrontEndHub> dashboardHubs =
        hubs.stream()
            .map(
                hub -> {
                  User.UserTemperatureUnit temperatureUnit = User.resolveUnit(hub.getOwner());

                  List<FrontEndProbe> probes =
                      probeService.getProbesByHubId(hub.getId()).stream()
                          .map(
                              probe -> {
                                Float currentTempCelsius =
                                    probeService.getCurrentTemperature(probe.getId());
                                Float targetTempCelsius = probe.getTargetTemp();

                                Float currentTemp =
                                    temperatureConversionService.toUserUnit(
                                        currentTempCelsius, temperatureUnit);
                                Float targetTemp =
                                    temperatureConversionService.toUserUnit(
                                        targetTempCelsius, temperatureUnit);

                                return new FrontEndProbe(
                                    probe.getId(),
                                    probe.getLocalId(),
                                    targetTemp,
                                    currentTemp,
                                    probe.getName(),
                                    probe.getColour(),
                                    (currentTemp == null || currentTemp.isNaN() ? false : true),
                                    probe.getVisible());
                              })
                          .toList();

                  final boolean connected = probes.stream().anyMatch(p -> p.getConnected());
                  return new FrontEndHub(
                      hub.getId(), hub.getName(), probes, connected, hub.getVisible());
                })
            .collect(Collectors.toCollection(ArrayList::new));

    // Add demo hub if enabled for user
    if (hubs.getFirst().getOwner().getDemoHubEnabled().booleanValue()) {
      LOG.info("Adding demo hub for user ID: {}", email);
      FrontEndHub frontEndDemoHub = demoService.getDemoHub();
      User.UserTemperatureUnit temperatureUnit = User.resolveUnit(hubs.getFirst().getOwner());

      List<FrontEndProbe> convertedProbes =
          frontEndDemoHub.getProbes().stream()
              .map(
                  probe -> {
                    Float convertedCurrent =
                        temperatureConversionService.toUserUnit(
                            probe.getCurrentTemp(), temperatureUnit);
                    Float convertedTarget =
                        temperatureConversionService.toUserUnit(
                            probe.getTargetTemp(), temperatureUnit);
                    probe.setCurrentTemp(convertedCurrent);
                    probe.setTargetTemp(convertedTarget);
                    return probe;
                  })
              .toList();

      frontEndDemoHub.setProbes(convertedProbes);
      dashboardHubs.add(0, frontEndDemoHub);
    }

    LOG.info("Successfully got {} hubs for user ID: {}", dashboardHubs.size(), email);
    return dashboardHubs;
  }

  /**
   * Get the list of default probe colours defined on the Probe entity.
   *
   * <p>This allows the UI to stay in sync with backend defaults without duplicating the list of hex
   * values on the front-end.
   *
   * @return immutable list of hex colour strings.
   */
  public List<String> getDefaultProbeColours() {
    return Probe.getDefaultColours();
  }
}
