package com.grillgauge.api.services;

import java.time.Instant;
import java.util.Optional;

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
}
