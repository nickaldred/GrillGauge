package com.grillgauge.api.services;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.models.ProbeReading;
import com.grillgauge.api.domain.repositorys.ProbeRepository;

/**
 * Service class for managing probes and their readings.
 */
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
        List<Probe> probes = probeRepository.findByHubId(hubId);
        if (probes.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No probes found for hub ID: %s".formatted(hubId));
        }
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
     *                                 local ID and hubId is not found
     */
    @Transactional
    public Reading saveProbeReading(final ProbeReading probeReading, final Long hubId) {
        List<Probe> probes = getProbesByHubId(hubId);
        Probe probe = probes.stream()
                .filter(x -> x.getLocalId().equals(probeReading.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Probe with ID: %s and HubId: %s not found".formatted(probeReading.getId(), hubId)));
        return readingService.saveCurrentReading(probe, probeReading.getCurrentTemp());
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
        int deletedProbes = probeRepository.deleteAllByHubId(hubId);
        if (deletedProbes == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No probes found for hub ID: %s".formatted(hubId));
        }
        return deletedProbes;
    }

    /**
     * Delete the probe with the given probeId.
     * 
     * @param probeId the probeId to delete
     * @return the number of deleted probes
     * @throws ResponseStatusException with status 404 if no probe is found for the
     *                                 given probeId
     */
    @Transactional
    public int deleteProbe(final Long probeId) {
        int deletedProbe = probeRepository.deleteAllByHubId(probeId);
        if (deletedProbe == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No probe found for probe ID: %s".formatted(probeId));
        }
        return deletedProbe;
    }

    public Float getCurrentTemperature(final Long probeId) {
        LOG.info("Getting current temp for probeID: {}", probeId);
        Optional<Reading> reading = readingService.getLatestReading(probeId);
        if (reading.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No readings found for probe ID: %s".formatted(probeId));
        }

        Float currentTemp = reading.get().getCurrentTemp();
        Instant readingTime = reading.get().getTimeStamp();
        if (readingTime.isBefore(java.time.Instant.now().minusSeconds(300))) {
            currentTemp = null;
            LOG.warn("No current temp available for for probeID: {}, last reading was at: {}", probeId,
                    Timestamp.from(readingTime));
        } else {
            LOG.info("Successfully got current temp for probeID: {}, temp is: {}", probeId, currentTemp);
        }
        return currentTemp;
    }
}
