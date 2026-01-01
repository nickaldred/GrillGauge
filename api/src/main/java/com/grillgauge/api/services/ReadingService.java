package com.grillgauge.api.services;

import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.ReadingRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Service class for managing readings. */
@Service
public class ReadingService {

  private static final Logger LOG = LoggerFactory.getLogger(ReadingService.class);

  private final ReadingRepository readingRepository;
  private final DemoService demoService;
  private final TemperatureConversionService temperatureConversionService;

  public ReadingService(
      final ReadingRepository readingRepository,
      final DemoService demoService,
      final TemperatureConversionService temperatureConversionService) {
    this.readingRepository = readingRepository;
    this.demoService = demoService;
    this.temperatureConversionService = temperatureConversionService;
  }

  /**
   * Get the latest reading for the given probeId.
   *
   * @param probeId probeId to get the latest reading for
   * @return Optional containing the latest Reading if found, otherwise empty
   */
  public Optional<Reading> getLatestReading(Long probeId) {
    LOG.debug("Retrieving latest reading for probe ID: {}", probeId);
    Optional<Reading> latestReading =
        readingRepository.findTopByProbeIdOrderByTimeStampDesc(probeId);
    if (latestReading.isPresent()) {
      LOG.debug(
          "Successfully retrieved latest reading ID: {} for probe ID: {}",
          latestReading.get().getId(),
          probeId);
    } else {
      LOG.debug("No readings found for probe ID: {}", probeId);
    }
    return latestReading;
  }

  /**
   * Save the current reading for the given probeId with the current timestamp.
   *
   * @param probe the probe to save the reading for
   * @param currentTemp the current temperature to save
   * @return the saved Reading entity
   */
  @Transactional
  public Reading saveCurrentReading(final Probe probe, final float currentTemp) {
    LOG.debug("Saving current reading for probe ID: {}", probe.getId());
    Reading reading = new Reading(probe, currentTemp, probe.getOwner().getReadingExpiry());
    readingRepository.save(reading);
    LOG.debug("Successfully saved reading ID: {} for probe ID: {}", reading.getId(), probe.getId());
    return reading;
  }

  /**
   * Delete all readings for the given probeId.
   *
   * @param probeId probeId to delete readings for
   * @return the number of deleted readings
   * @throws ResponseStatusException with status 404 if no readings are found for the given probeId
   */
  @Transactional
  public Long deleteAllReadings(final Long probeId) {
    LOG.debug("Deleting all readings for probe ID: {}", probeId);
    Long deletedReadings = readingRepository.deleteAllByProbeId(probeId);
    if (deletedReadings == 0) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "No readings found for probe ID: %s".formatted(probeId));
    }
    LOG.debug("Successfully deleted {} readings for probe ID: {}", deletedReadings, probeId);
    return deletedReadings;
  }

  /**
   * Get all readings for the given probe between the specified start and end timestamps.
   *
   * @param probeId the ID of the probe
   * @param start the start timestamp in ISO 8601 format
   * @param end the end timestamp in ISO 8601 format
   * @return list of Reading entities between the specified timestamps
   * @throws ResponseStatusException with status 400 if the date format is invalid
   */
  public List<Reading> getReadingsForProbeBetween(Long probeId, String start, String end) {
    LOG.info("Getting readings for probeID: {}, between: {} - {}", probeId, start, end);
    try {
      Instant startInstant = Instant.parse(start);
      Instant endInstant = Instant.parse(end);

      if (probeId < 0) {
        return getDemoReadingsBetween(probeId, startInstant, endInstant);
      }

      return getRealReadingsBetween(probeId, startInstant, endInstant);
    } catch (Exception e) {
      String message =
          "Invalid date format for start: %s or end: %s, please use ISO 8601 format."
              .formatted(start, end);
      LOG.error(message);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
  }

  /**
   * Get readings for a probe between timestamps, converted to the user's preferred unit.
   *
   * @param probeId the probe ID
   * @param start start timestamp ISO-8601
   * @param end end timestamp ISO-8601
   * @param unit preferred unit; defaults to Celsius when null
   * @return list of lightweight views with converted temperatures
   */
  public List<ReadingView> getReadingsForProbeBetweenConverted(
      Long probeId, String start, String end, User.UserTemperatureUnit unit) {
    User.UserTemperatureUnit resolvedUnit = unit == null ? User.UserTemperatureUnit.CELSIUS : unit;

    return getReadingsForProbeBetween(probeId, start, end).stream()
        .map(
            r ->
                new ReadingView(
                    r.getTimeStamp(),
                    temperatureConversionService.toUserUnit(r.getCurrentTemp(), resolvedUnit)))
        .toList();
  }

  /** Immutable view of a reading with converted temperature. */
  public record ReadingView(Instant timestamp, Float temperature) {}

  private List<Reading> getDemoReadingsBetween(
      Long probeId, Instant startInstant, Instant endInstant) {
    List<Reading> demoReadings =
        demoService.getReadingsForProbeBetween(probeId, startInstant, endInstant).stream()
            .map(r -> new Reading(null, null, r.timestamp(), r.timestamp(), r.temperature()))
            .toList();
    LOG.info("Successfully got {} demo readings for probeID: {}", demoReadings.size(), probeId);
    return demoReadings;
  }

  private List<Reading> getRealReadingsBetween(
      Long probeId, Instant startInstant, Instant endInstant) {
    List<Reading> probeReadings =
        readingRepository.findByProbe_IdAndTimeStampBetweenOrderByTimeStampAsc(
            probeId, startInstant, endInstant);
    LOG.info("Successfully got {} readings for probeID: {}", probeReadings.size(), probeId);
    return probeReadings;
  }
}
