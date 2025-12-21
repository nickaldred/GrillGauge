package com.grillgauge.api.services;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.domain.repositorys.HubRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Service class for managing hubs and their readings. */
@Service
public class HubService {
  private static final Logger LOG = LoggerFactory.getLogger(HubService.class);

  private HubRepository hubRepository;

  public HubService(final HubRepository hubRepository) {
    this.hubRepository = hubRepository;
  }

  /**
   * Get the hub for the given hubId.
   *
   * @param hubId hubId to get the hub for.
   * @return Hub entity.
   * @throws ResponseStatusException with status 404 if no hub is found for the
   *                                 given hubId.
   */
  public Hub getHub(final Long hubId) {
    LOG.info("Retrieving hub for hub ID: {}", hubId);
    Optional<Hub> hub = hubRepository.findById(hubId);
    if (hub.isEmpty()) {
      final String message = "No hub found for hub ID: %s".formatted(hubId);
      LOG.error(message);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, message);
    }
    LOG.info("Successfully retrieved hub for hub ID: {}", hubId);
    return hub.get();
  }

  /**
   * Store a new hub.
   *
   * @param hub the Hub entity to store.
   * @return the stored Hub entity.
   */
  @NonNull
  @Transactional
  public Hub storeHub(final Hub hub) {
    LOG.info("Storing new Hub for owner: {}", hub.getId());
    Hub storedHub = hubRepository.save(hub);
    LOG.info("Successfully stored Hub with ID: {}", storedHub.getId());
    return storedHub;
  }

  /**
   * Delete a hub by its ID.
   *
   * @param hubId The ID of the hub to delete.
   */
  @Transactional
  public void deleteHub(final Long hubId) {
    LOG.info("Attempting to delete hub ID: {}", hubId);
    hubRepository.deleteById(hubId);
    LOG.info("Successfully deleted hub ID: {}", hubId);
  }

  /**
   * Get all hubs for the given email address.
   *
   * @param email email address to get the hubs for.
   * @return List of Hub entities.
   */
  public List<Hub> getHubsByEmail(final String email) {
    LOG.info("Retrieving hubs for email: {}", email);
    List<Hub> hubs = hubRepository.findByOwnerEmail(email);
    LOG.info("Found {} hubs for email: {}", hubs.size(), email);
    return hubs;
  }

  /**
   * Update a hub.
   *
   * @param hubToUpdate hub to update.
   * @return updated hub.
   */
  public FrontEndHub updateHub(final FrontEndHub hubToUpdate) {
    LOG.info("Updating Hub with ID: {}", hubToUpdate.getId());
    Hub existingHub = this.getHub(hubToUpdate.getId());
    existingHub.setName(hubToUpdate.getName());
    this.storeHub(existingHub);
    LOG.info("Successfully updated Hub with ID: {}", hubToUpdate.getId());
    return hubToUpdate;
  }
}
