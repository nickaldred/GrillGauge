package com.grillgauge.api.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.services.ReadingService;

import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/probe")
public class ProbeController {

    private final ReadingService readingService;

    public ProbeController(ReadingService readingService) {
        this.readingService = readingService;
    }

    @GetMapping("/{probeId}/readings/between")
    public List<Reading> getReadingsForProbeBetween(@PathVariable Long probeId, @Param("start") String start,
            @Param("end") String end) {
        return readingService.getReadingsForProbeBetween(probeId, start, end);
    }
}
