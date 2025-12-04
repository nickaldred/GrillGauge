package com.grillgauge.api.domain.models;

public record HubRegistrationRequest(
        String model,
        String fwVersion) {
}
