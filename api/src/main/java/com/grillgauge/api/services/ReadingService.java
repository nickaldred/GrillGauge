package com.grillgauge.api.services;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.repositorys.ReadingRepository;

/**
 * Service class for managing readings.
 */
@Service
public class ReadingService {

    private static final Logger LOG = LoggerFactory.getLogger(ReadingService.class);

    private ReadingRepository readingRepository;

    public ReadingService(final ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    /**
     * Get the latest reading for the given probeId.
     * 
     * @param probeId probeId to get the latest reading for
     * @return Optional containing the latest Reading if found, otherwise empty
     */
    public Optional<Reading> getLatestReading(Long probeId) {
        return readingRepository.findTopByProbeIdOrderByTimeStampDesc(probeId);
    }

    /**
     * Save the current reading for the given probeId with the current timestamp.
     * 
     * @param probe       the probe to save the reading for
     * @param currentTemp the current temperature to save
     * @return the saved Reading entity
     */
    @Transactional
    public Reading saveCurrentReading(final Probe probe, final float currentTemp) {
        Reading reading = new Reading(probe, currentTemp);
        readingRepository.save(reading);
        return reading;
    }

    /**
     * Delete all readings for the given probeId.
     * 
     * @param probeId probeId to delete readings for
     * @return the number of deleted readings
     * @throws ResponseStatusException with status 404 if no readings are found for
     *                                 the given probeId
     */
    @Transactional
    public Long deleteAllReadings(final Long probeId) {
        Long deletedReadings = readingRepository.deleteAllByProbeId(probeId);
        if (deletedReadings == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No readings found for probe ID: %s".formatted(probeId));
        }
        return deletedReadings;
    }

    /**
     * Get all readings for the given probe between the specified start and end
     * timestamps.
     * 
     * @param probe the probe to get readings for
     * @param start the start timestamp in ISO 8601 format
     * @param end   the end timestamp in ISO 8601 format
     * @return list of Reading entities between the specified timestamps
     * @throws ResponseStatusException with status 400 if the date format is invalid
     */
    public List<Reading> getReadingsForProbeBetween(Long probeId, String start, String end) {
        LOG.info("Getting readings for probeID: {}, between: {} - {}", probeId, start, end);
        try {
            return readingRepository.findByProbe_IdAndTimeStampBetweenOrderByTimeStampAsc(
                    probeId,
                    Instant.parse(start),
                    Instant.parse(end));
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid date format. Please use ISO 8601 format (e.g., 2023-10-01T12:00:00Z).");
        }
    }
}
