package com.grillgauge.api.controllers;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.services.ExternalHubService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Controller for managing communication with the external hubs.
 * 
 * Provides endpoints for storing readings and retrieving the current state of a
 * hub, identified by an API key provided in the request.
 */
@RestController()
@RequestMapping("/api/v1/externalHub")
public class ExternalHubController {

    private ExternalHubService externalHubService;

    public ExternalHubController(final ExternalHubService externalHubService) {
        this.externalHubService = externalHubService;
    }

    /**
     * Extract the hub ID from the request attributes, which should have been set by
     * the API key authentication filter.
     * 
     * @param request The HTTP request.
     * @return The hub ID associated with the API key in the request.
     * @throws ResponseStatusException If the hub ID is not found in the request,
     *                                 indicating an invalid or missing API key.
     */
    private Long getHubId(HttpServletRequest request) {
        Long hubId = (Long) request.getAttribute("hubId");
        if (hubId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
        }
        return hubId;
    }

    /**
     * Store a new reading for the hub identified by the API key in the request.
     * 
     * @param reading The reading to store.
     * @param request The HTTP request, used to extract the hub ID from the API key.
     * @return The stored reading, including any additional data added by the
     *         service.
     */
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) // 201
    public HubReading storeReading(@RequestBody HubReading reading, HttpServletRequest request) {
        final Long hubId = getHubId(request);
        return externalHubService.saveHubReading(reading, hubId);
    }

    /**
     * Get the current state of the hub identified by the API key in the request.
     * 
     * @param request The HTTP request, used to extract the hub ID from the API key.
     * @return The current state of the hub, including latest readings and status.
     */
    @GetMapping()
    public HubCurrentState getCurrentState(HttpServletRequest request) {
        final Long hubId = getHubId(request);
        return externalHubService.getHubCurrentState(hubId);
    }

}
