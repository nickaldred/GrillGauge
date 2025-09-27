package com.grillgauge.api.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.services.HubService;

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

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) // 201
    public HubReading storeReading(@RequestBody HubReading reading) {
        return hubService.saveHubReading(reading);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.ACCEPTED) // 200
    public String getCurrentState() {
        return new String();
    }

}
