package com.grillgauge.api.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.services.FrontEndService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/ui")
public class FrontEndController {

    private final FrontEndService frontEndService;

    public FrontEndController(FrontEndService frontEndService) {
        this.frontEndService = frontEndService;
    }

    @GetMapping("/hubs")
    public List<FrontEndHub> getHubs(@RequestParam String email) {
        return frontEndService.getHubs(email);
    }

}
