package com.grillgauge.api.services;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.domain.models.ProbeReading;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Service class for managing hubs and their readings. */
@Service
@PreAuthorize("hasRole('HUB')")
public class ExternalHubService {
  private static final Logger LOG = LoggerFactory.getLogger(ExternalHubService.class);

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
   * @param hubId the hubId to which the hub belongs
   * @return the saved HubReading entity
   */
  @Transactional
  public HubReading saveHubReading(final HubReading hubReading, final Long hubId) {
    LOG.info("Saving hub reading for hubId: {}", hubId);
    for (ProbeReading probeReading : hubReading.getProbeReadings()) {
      probeService.saveProbeReading(probeReading, hubId);
    }
    LOG.info("Successfully saved hub reading for hubId: {}", hubId);
    return hubReading;
  }

  /**
   * Get the current state of the hub for the given hubId, including its probes.
   *
   * @param hubId hubId to get the current state for
   * @return HubCurrentState containing the hubId, hub name, and list of probes
   */
  public HubCurrentState getHubCurrentState(final Long hubId) {
    LOG.info("Fetching current state for hubId: {}", hubId);
    Hub hub = hubService.getHub(hubId);
    List<Probe> probes = probeService.getProbesByHubId(hubId);
    LOG.info("Fetched {} probes for hubId: {}", probes.size(), hubId);
    return new HubCurrentState(hubId, hub.getName(), probes);
  }
}
