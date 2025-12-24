package com.grillgauge.api.domain.repositorys;

import com.grillgauge.api.domain.entitys.Probe;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for managing Probe entities. */
@Repository
public interface ProbeRepository extends JpaRepository<Probe, Long> {

  List<Probe> findByHubId(Long hubId);

  int deleteAllByHubId(Long hubId);

  boolean existsByIdAndOwnerEmail(Long id, String email);

  long countByIdInAndOwnerEmail(List<Long> ids, String email);
}
