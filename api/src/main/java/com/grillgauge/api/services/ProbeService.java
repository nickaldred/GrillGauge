package com.grillgauge.api.services;

import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.models.FrontEndProbe;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Service class for managing probes and their readings. */
@Service
public class ProbeService {
  private static final Logger LOG = LoggerFactory.getLogger(ProbeService.class);

  private ReadingService readingService;
  private ProbeRepository probeRepository;

  public ProbeService(final ProbeRepository probeRepository, final ReadingService readingService) {
    this.probeRepository = probeRepository;
    this.readingService = readingService;
  }

  /**
   * Get all probes for the given hubId.
   *
   * @param hubId hubId to get probes for
   * @return List of Probe entities
   * @throws ResponseStatusException with status 404 if no probes are found for
   *                                 the given hubId
   */
  public List<Probe> getProbesByHubId(final Long hubId) {
    LOG.info("Retrieving probes for hub ID: {}", hubId);
    List<Probe> probes = probeRepository.findByHubId(hubId);
    if (probes.isEmpty()) {
      final String message = "No probes found for hub ID: %s".formatted(hubId);
      LOG.warn(message);
    }
    LOG.info("Successfully retrieved {} probes for hub ID: {}", probes.size(), hubId);
    return probes;
  }

  /**
   * Save a probe reading for the given hubId.
   *
   * @param probeReading the ProbeReading containing the local probe ID and
   *                     current temperature
   * @param hubId        the hubId to which the probe belongs
   * @return the saved Reading entity
   * @throws ResponseStatusException with status 404 if the probe with the given
   *                                 local ID and hubId
   *                                 is not found
   */
  @Transactional
  public Reading saveProbeReading(final ProbeReading probeReading, final Long hubId) {
    LOG.debug(
        "Saving probe reading for probe ID: {} under hub ID: {}", probeReading.getId(), hubId);
    List<Probe> probes = getProbesByHubId(hubId);
    Probe probe = probes.stream()
        .filter(x -> x.getLocalId().equals(probeReading.getId()))
        .findFirst()
        .orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Probe with ID: %s and HubId: %s not found"
                    .formatted(probeReading.getId(), hubId)));
    Reading savedReading = readingService.saveCurrentReading(probe, probeReading.getCurrentTemp());
    LOG.debug(
        "Successfully saved reading for probe ID: {} under hub ID: {}",
        probeReading.getId(),
        hubId);
    return savedReading;
  }

  /**
   * Delete all probes for the given hubId.
   *
   * @param hubId the hubId to delete probes for
   * @return the number of deleted probes
   * @throws ResponseStatusException with status 404 if no probes are found for
   *                                 the given hubId
   */
  @Transactional
  public int deleteAllProbesForHubId(final Long hubId) {
    LOG.info("Deleting all probes for hub ID: {}", hubId);
    int deletedProbes = probeRepository.deleteAllByHubId(hubId);
    if (deletedProbes == 0) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "No probes found for hub ID: %s".formatted(hubId));
    }
    LOG.info("Successfully deleted {} probes for hub ID: {}", deletedProbes, hubId);
    return deletedProbes;
  }

  /**
   * Delete the probe with the given probeId.
   *
   * @param probeId the probeId to delete
   * @throws ResponseStatusException with status 404 if no probe is found for the
   *                                 given probeId
   */
  @Transactional
  public void deleteProbe(final Long probeId) {
    LOG.info("Deleting probe for probe ID: {}", probeId);
    probeRepository.deleteById(probeId);
  }

  /**
   * Get the current temperature for the given probeId.
   *
   * @param probeId the probeId to get the current temperature for
   * @return the current temperature, or null if no recent reading is available
   * @throws ResponseStatusException with status 404 if no readings are found for
   *                                 the given probeId
   */
  public Float getCurrentTemperature(final Long probeId) {
    LOG.info("Getting current temp for probeID: {}", probeId);
    Optional<Reading> reading = readingService.getLatestReading(probeId);
    if (reading.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "No readings found for probe ID: %s".formatted(probeId));
    }

    Float currentTemp = reading.get().getCurrentTemp();
    Instant readingTime = reading.get().getTimeStamp();
    if (readingTime.isBefore(java.time.Instant.now().minusSeconds(300))) {
      currentTemp = null;
      LOG.warn(
          "No current temp available for for probeID: {}, last reading was at: {}",
          probeId,
          Timestamp.from(readingTime));
    } else {
      LOG.info("Successfully got current temp for probeID: {}, temp is: {}", probeId, currentTemp);
    }
    return currentTemp;
  }

  /**
   * Update the probe with the given FrontEndProbe data.
   *
   * @param frontEndProbe the FrontEndProbe containing updated probe data.
   */
  public void updateProbe(final FrontEndProbe frontEndProbe) {
    LOG.info("Updating probe with ID: {}", frontEndProbe.getId());
    Probe probe = probeRepository
        .findById(frontEndProbe.getId())
        .orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No probe found for probe ID: %s".formatted(frontEndProbe.getId())));

    probe.setName(frontEndProbe.getName());
    probe.setTargetTemp(frontEndProbe.getTargetTemp());

    probeRepository.save(probe);
    LOG.info("Successfully updated probe with ID: {}", frontEndProbe.getId());
  }

  /**
   * Update the target temperature of a probe.
   *
   * @param probeId    The ID of the probe to update.
   * @param targetTemp The new target temperature.
   * @return The updated target temperature.
   */
  public float updateTargetTemp(final long probeId, final float targetTemp) {
    LOG.info("Updating target temperature for probe ID: {} to {}", probeId, targetTemp);
    Probe probe = probeRepository
        .findById(probeId)
        .orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No probe found for probe ID: %s".formatted(probeId)));

    probe.setTargetTemp(targetTemp);
    probeRepository.save(probe);
    LOG.info("Successfully updated target temperature for probe ID: {} to {}", probeId, targetTemp);
    return targetTemp;
  }

  /**
   * Update the name of a probe.
   *
   * @param probeId The probe ID.
   * @param name    The name to update.
   * @return The updated name of the probe.
   */
  public Map<String, Object> updateProbeName(final long probeId, final String name) {
    LOG.info("Updating name for probe ID: {} to {}", probeId, name);
    Probe probe = probeRepository
        .findById(probeId)
        .orElseThrow(
            () -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No probe found for probe ID: %s".formatted(probeId)));

    probe.setName(name);
    probeRepository.save(probe);
    LOG.info("Successfully updated name for probe ID: {} to {}", probeId, name);
    return Map.of("probeId", probeId, "name", name);
  }
}
