package com.grillgauge.api.services;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * @param probeId     probeId to save the reading for
     * @param currentTemp the current temperature to save
     * @return the saved Reading entity
     */
    @Transactional
    public Reading saveCurrentReading(final Long probeId, final float currentTemp) {
        Reading reading = new Reading(probeId, Instant.now(), currentTemp);
        readingRepository.save(reading);
        return reading;
    }
}
