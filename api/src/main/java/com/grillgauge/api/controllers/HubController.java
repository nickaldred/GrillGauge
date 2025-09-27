package com.grillgauge.api.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.services.HubService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController()
@RequestMapping("/api/v1/hub")
public class HubController {

    private final HubService hubService;

    public HubController(final HubService hubService) {
        this.hubService = hubService;
    }

    private Long getHubId(HttpServletRequest request) {
        Long hubId = (Long) request.getAttribute("hubId");
        if (hubId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        return hubId;
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) // 201
    public HubReading storeReading(@RequestBody HubReading reading, HttpServletRequest request) {
        final Long hubId = getHubId(request);
        return hubService.saveHubReading(reading, hubId);
    }

    @GetMapping()
    public HubCurrentState getCurrentState(HttpServletRequest request) {
        final Long hubId = getHubId(request);
        return hubService.getHubCurrentState(hubId);
    }

}
