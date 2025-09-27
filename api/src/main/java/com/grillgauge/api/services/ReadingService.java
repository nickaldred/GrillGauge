package com.grillgauge.api.services;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.repositorys.ReadingRepository;

@Service
public class ReadingService {

    private ReadingRepository readingRepository;

    public ReadingService(final ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }

    public Optional<Reading> getLatestReading(Long probeId) {
        return readingRepository.findTopByProbeIdOrderByTimeStampDesc(probeId);
    }

    public Reading saveCurrentReading(final Long probeId, final float currentTemp) {
        Reading reading = new Reading(probeId, Instant.now(), currentTemp);
        readingRepository.save(reading);
        return reading;
    }

}
