package com.grillgauge.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.services.HubService;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for managing hub-related operations.
 * 
 * Provides endpoints for storing readings and retrieving the current state of a
 * hub, identified by an API key provided in the request.
 */
@RestController()
@RequestMapping("/api/v1/hub")
public class HubController {

    private HubService hubService;

    public HubController(final HubService hubService) {
        this.hubService = hubService;
    }

    @GetMapping()
    public Hub getHub(@RequestBody long hubId) {
        return this.hubService.getHub(hubId);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) // 201
    public Hub storeHub(@RequestBody Hub hub) {
        return this.hubService.storeHub(hub);

    }

    @DeleteMapping()
    public void deleteHub(@RequestBody long hubId) {
        hubService.deleteHub(hubId);
    }
}
