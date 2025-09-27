package com.grillgauge.api.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubKey;
import com.grillgauge.api.domain.models.HubReading;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController()
@RequestMapping("/api/v1/hub")
public class HubController {

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED) // 201
    public HubReading storeReading(@RequestBody HubReading reading) {

        return new HubReading();
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.ACCEPTED) // 200
    public String getCurrentState() {
        return new String();
    }

}
