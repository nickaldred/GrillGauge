package com.grillgauge.api.domain.models;

import java.time.Instant;

public record HubRegistrationResponse(
                Long hubId,
                String otp,
                Instant otpExpiresAt) {
}
