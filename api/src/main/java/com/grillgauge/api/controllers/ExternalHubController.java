package com.grillgauge.api.controllers;

import com.grillgauge.api.domain.models.HubCurrentState;
import com.grillgauge.api.domain.models.HubReading;
import com.grillgauge.api.security.HubUserDetails;
import com.grillgauge.api.services.ExternalHubService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing communication with the external hubs. Provides endpoints for storing
 * readings and retrieving the current state of a hub, identified by an API key provided in the
 * request.
 */
@RestController()
@RequestMapping("/api/v1/externalHub")
@PreAuthorize("hasRole('HUB')")
public class ExternalHubController {

  private ExternalHubService externalHubService;

  public ExternalHubController(final ExternalHubService externalHubService) {
    this.externalHubService = externalHubService;
  }

  /**
   * Store a new reading for the hub identified by the API key in the request.
   *
   * @param reading The reading to store.
   * @return The stored reading, including any additional data added by the service.
   */
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED)
  public HubReading storeReading(
      @RequestBody HubReading reading, @AuthenticationPrincipal HubUserDetails hubPrincipal) {
    final Long hubId = hubPrincipal.getHubId();
    return externalHubService.saveHubReading(reading, hubId);
  }

  /**
   * Get the current state of the hub identified by the API key in the request.
   *
   * @return The current state of the hub, including latest readings and status.
   */
  @GetMapping()
  public HubCurrentState getCurrentState(@AuthenticationPrincipal HubUserDetails hubPrincipal) {
    final Long hubId = hubPrincipal.getHubId();
    return externalHubService.getHubCurrentState(hubId);
  }
}
