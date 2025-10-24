package com.grillgauge.api.controllers;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.services.ReadingService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/probe")
public class ProbeController {

    private final ReadingService readingService;

    public ProbeController(ReadingService readingService) {
        this.readingService = readingService;
    }

    public record ReadingDTO(Instant timestamp, double temperature) {
    }

    @GetMapping("/{probeId}/readings/between")
    public List<ReadingDTO> getReadingsForProbeBetween(@PathVariable Long probeId, @RequestParam("start") String start,
            @RequestParam("end") String end) {
        return readingService.getReadingsForProbeBetween(probeId, start, end)
                .stream()
                .map(r -> new ReadingDTO(r.getTimeStamp(), r.getCurrentTemp()))
                .toList();
    }
}
