package com.grillgauge.api.controllers;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.services.ReadingService;

import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for managing probe-related operations.
 * 
 * Provides endpoints for retrieving readings for probes.
 */
@RestController
@RequestMapping("/api/v1/probe")
public class ProbeController {

    private final ReadingService readingService;

    public ProbeController(ReadingService readingService) {
        this.readingService = readingService;
    }

    /**
     * Data Transfer Object for probe readings.
     */
    public record ReadingDTO(Instant timestamp, double temperature) {
    }

    /**
     * Get readings for multiple probes between the specified start and end times.
     * 
     * @param probeIds array of probe IDs to get readings for.
     * @param start    start time in ISO-8601 format.
     * @param end      end time in ISO-8601 format.
     * @return map of probe IDs to their list of ReadingDTOs.
     */
    @GetMapping("/readings/between")
    public Map<Long, List<ReadingDTO>> getReadingsForProbeBetween(
            @RequestParam("probeIds") Long[] probeIds,
            @RequestParam("start") String start,
            @RequestParam("end") String end) {

        return Stream.of(probeIds)
                .collect(Collectors.toMap(
                        probeId -> probeId,
                        probeId -> readingService.getReadingsForProbeBetween(probeId, start, end)
                                .stream()
                                .map(r -> new ReadingDTO(r.getTimeStamp(), r.getCurrentTemp()))
                                .toList()));
    }
}
