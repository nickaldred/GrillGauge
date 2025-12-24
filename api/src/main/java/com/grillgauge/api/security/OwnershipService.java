package com.grillgauge.api.security;

import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

/** Helper service for ownership checks used in method security expressions. */
@Service
public class OwnershipService {

  private final HubRepository hubRepository;
  private final ProbeRepository probeRepository;

  public OwnershipService(HubRepository hubRepository, ProbeRepository probeRepository) {
    this.hubRepository = hubRepository;
    this.probeRepository = probeRepository;
  }

  /**
   * Check if the user with the given email can access the hub with the given hubId.
   *
   * @param hubId The ID of the hub to check access for.
   * @param email The email of the user to check access for.
   * @return true if the user can access the hub, false otherwise.
   */
  public boolean canAccessHub(Long hubId, String email) {
    if (hubId == null || email == null || email.isBlank()) {
      return false;
    }
    return hubRepository.existsByIdAndOwnerEmail(hubId, email);
  }

  /**
   * Check if the user with the given email can access the probe with the given probeId.
   *
   * @param probeId The ID of the probe to check access for.
   * @param email The email of the user to check access for.
   * @return true if the user can access the probe, false otherwise.
   */
  public boolean canAccessProbe(Long probeId, String email) {
    if (probeId == null || email == null || email.isBlank()) {
      return false;
    }
    return probeRepository.existsByIdAndOwnerEmail(probeId, email);
  }

  /**
   * Check if the user with the given email can access all probes with the given probeIds.
   *
   * @param probeIds The IDs of the probes to check access for.
   * @param email The email of the user to check access for.
   * @return true if the user can access all probes, false otherwise.
   */
  public boolean canAccessAllProbes(Long[] probeIds, String email) {
    if (probeIds == null || probeIds.length == 0 || email == null || email.isBlank()) {
      return false;
    }
    List<Long> ids = Arrays.asList(probeIds);
    long ownedCount = probeRepository.countByIdInAndOwnerEmail(ids, email);
    return ownedCount == ids.size();
  }
}
