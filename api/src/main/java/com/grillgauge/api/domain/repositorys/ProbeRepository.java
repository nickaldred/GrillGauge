package com.grillgauge.api.domain.repositorys;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.grillgauge.api.domain.entitys.Probe;

/**
 * Repository interface for managing Probe entities.
 */
@Repository
public interface ProbeRepository extends JpaRepository<Probe, Long> {

    List<Probe> findByHubId(Long hubId);

    int deleteAllByHubId(Long hubId);
}
