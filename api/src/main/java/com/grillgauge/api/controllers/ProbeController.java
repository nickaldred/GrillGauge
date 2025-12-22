package com.grillgauge.api.controllers;

import com.grillgauge.api.domain.models.FrontEndProbe;
import com.grillgauge.api.services.ProbeService;
import com.grillgauge.api.services.ReadingService;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing probe-related operations. Provides endpoints for retrieving readings for
 * probes.
 */
@RestController
@RequestMapping("/api/v1/probe")
public class ProbeController {

  private final ReadingService readingService;
  private final ProbeService probeService;

  public ProbeController(final ReadingService readingService, final ProbeService probeService) {
    this.readingService = readingService;
    this.probeService = probeService;
  }

  /** Data Transfer Object for probe readings. */
  public record ReadingDto(Instant timestamp, double temperature) {}

  /**
   * Get readings for multiple probes between the specified start and end times.
   *
   * @param probeIds array of probe IDs to get readings for.
   * @param start start time in ISO-8601 format.
   * @param end end time in ISO-8601 format.
   * @return map of probe IDs to their list of ReadingDTOs.
   */
  @GetMapping("/readings/between")
  public Map<Long, List<ReadingDto>> getReadingsForProbesBetween(
      @RequestParam() Long[] probeIds, @RequestParam() String start, @RequestParam() String end) {

    return Stream.of(probeIds)
        .collect(
            Collectors.toMap(
                probeId -> probeId,
                probeId ->
                    readingService.getReadingsForProbeBetween(probeId, start, end).stream()
                        .map(r -> new ReadingDto(r.getTimeStamp(), r.getCurrentTemp()))
                        .toList()));
  }

  /**
   * Update a probe.
   *
   * @param probe The probe to update.
   * @return The updated probe.
   */
  @PutMapping()
  public FrontEndProbe updateProbe(@RequestBody FrontEndProbe probe) {
    probeService.updateProbe(probe);
    return probe;
  }

  /**
   * Update the target temperature of a probe.
   *
   * @param probeId The ID of the probe to update.
   * @param targetTemp The new target temperature.
   * @return The updated target temperature.
   */
  @PutMapping("targetTemp/{probeId}")
  public float updateTargetTemp(@PathVariable Long probeId, @RequestParam float targetTemp) {
    return probeService.updateTargetTemp(probeId, targetTemp);
  }

  /**
   * Updates the name of a probe.
   *
   * @param probeId The ID of the probe to update.
   * @param name The updated name of the probe.
   * @return The updated name of the probe.
   */
  @PutMapping("name/{probeId}")
  public Map<String, Object> updateProbeName(
      @PathVariable Long probeId, @RequestParam String name) {
    return probeService.updateProbeName(probeId, name);
  }

  /**
   * Delete a probe by its ID.
   *
   * @param probeId The ID of the probe to delete.
   */
  @DeleteMapping("/{probeId}")
  public void deleteProbe(@PathVariable Long probeId) {
    probeService.deleteProbe(probeId);
  }
}
