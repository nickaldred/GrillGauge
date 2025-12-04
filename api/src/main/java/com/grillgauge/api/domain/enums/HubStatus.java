package com.grillgauge.api.domain.enums;

public enum HubStatus {
    PENDING, // OTP generated, waiting for user confirmation
    CONFIRMED, // OTP validated, waiting for CSR
    REGISTERED, // Certificate issued
    REVOKED // Device no longer trusted
}
