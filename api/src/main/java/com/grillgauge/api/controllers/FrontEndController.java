package com.grillgauge.api.controllers;

import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.services.FrontEndService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for managing front-end related operations.
 *
 * <p>Provides endpoints for retrieving hubs associated with a user.
 */
@RestController
@RequestMapping("/api/v1/ui")
public class FrontEndController {

  private final FrontEndService frontEndService;

  public FrontEndController(FrontEndService frontEndService) {
    this.frontEndService = frontEndService;
  }

  /**
   * Get the list of hubs associated with the given email.
   *
   * @param email the email to get hubs for.
   * @return list of FrontEndHub models.
   */
  @GetMapping("/hubs")
  public List<FrontEndHub> getHubs(@RequestParam String email) {
    return frontEndService.getHubs(email);
  }
}
