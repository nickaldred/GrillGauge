package com.grillgauge.api.services;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.repositorys.HubRepository;

/**
 * Service class for managing hubs and their readings.
 */
@Service
public class HubService {

    private HubRepository hubRepository;

    public HubService(final HubRepository hubRepository) {
        this.hubRepository = hubRepository;
    }

    /**
     * Get the hub for the given hubId.
     * 
     * @param hubId hubId to get the hub for
     * @return Hub entity
     * @throws ResponseStatusException with status 404 if no hub is found for the
     *                                 given hubId
     */
    public Hub getHub(final Long hubId) {
        Optional<Hub> hub = hubRepository.findById(hubId);
        if (hub.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No hub found for hub ID: %s".formatted(hubId));
        }
        return hub.get();
    }

    /**
     * Store a new hub.
     * 
     * @param hub the Hub entity to store
     * @return the stored Hub entity
     */
    @Transactional
    public Hub storeHub(final Hub hub) {
        return hubRepository.save(hub);
    }

    /**
     * Store a new hub.
     * 
     * @param hub the Hub entity to store
     * @return the stored Hub entity
     */
    @Transactional
    public void deleteHub(final Long hubId) {
        hubRepository.deleteById(hubId);
    }
}
