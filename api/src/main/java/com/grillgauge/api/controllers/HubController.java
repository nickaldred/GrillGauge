package com.grillgauge.api.controllers;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.services.HubService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing hub-related operations. Provides endpoints for storing readings and
 * retrieving the current state of a hub, identified by an API key provided in the request.
 */
@RestController()
@RequestMapping("/api/v1/hub")
public class HubController {

  private HubService hubService;

  public HubController(final HubService hubService) {
    this.hubService = hubService;
  }

  /**
   * Get the hub for the given hubId.
   *
   * @param hubId hubId to get the hub for.
   * @return Hub entity.
   */
  @GetMapping()
  @PreAuthorize("@ownershipService.canAccessHub(#hubId, authentication.name) or hasRole('ADMIN')")
  public Hub getHub(@RequestParam long hubId) {
    return this.hubService.getHub(hubId);
  }

  /**
   * Store a new hub.
   *
   * @param hub the Hub entity to store.
   * @return the stored Hub entity.
   */
  @PostMapping()
  @ResponseStatus(HttpStatus.CREATED) // 201
  @PreAuthorize("hasRole('ADMIN')")
  public Hub storeHub(@RequestBody Hub hub) {
    return this.hubService.storeHub(hub);
  }

  /**
   * Delete a hub by its ID.
   *
   * @param hubId the ID of the hub to delete.
   */
  @DeleteMapping("/{hubId}")
  @PreAuthorize("@ownershipService.canAccessHub(#hubId, authentication.name) or hasRole('ADMIN')")
  public void deleteHub(@PathVariable long hubId) {
    hubService.deleteHub(hubId);
  }

  /**
   * Update a hub.
   *
   * @param hub the hub to update.
   * @return the updated hub.
   */
  @PutMapping()
  @PreAuthorize("@ownershipService.canAccessHub(#hub.id, authentication.name) or hasRole('ADMIN')")
  public FrontEndHub updateHub(@RequestBody FrontEndHub hub) {
    return this.hubService.updateHub(hub);
  }
}
