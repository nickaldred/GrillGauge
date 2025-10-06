package com.grillgauge.api.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.grillgauge.api.services.FrontEndService;

@RestController
@RequestMapping("/api/v1/ui")
public class FrontEndController {

    private final FrontEndService frontEndService;

    public FrontEndController(FrontEndService frontEndService) {
        this.frontEndService = frontEndService;
    }

}
