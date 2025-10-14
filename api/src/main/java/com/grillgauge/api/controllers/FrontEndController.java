package com.grillgauge.api.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.domain.models.Dashboard;
import com.grillgauge.api.services.FrontEndService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/ui")
public class FrontEndController {

    private final FrontEndService frontEndService;

    public FrontEndController(FrontEndService frontEndService) {
        this.frontEndService = frontEndService;
    }

    @GetMapping("/dashboard")
    public Dashboard getMethodName(@RequestParam long userId) {
        return frontEndService.getDashboard(userId);
    }

}
