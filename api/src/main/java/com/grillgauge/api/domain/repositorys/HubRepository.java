package com.grillgauge.api.domain.repositorys;

import com.grillgauge.api.domain.entitys.Hub;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for managing Hub entities. */
@Repository
public interface HubRepository extends JpaRepository<Hub, Long> {

  List<Hub> findByOwnerEmail(String email);

  Optional<Hub> findByCertificateSerial(Long certificateSerial);

  boolean existsByIdAndOwnerEmail(Long id, String email);
}
