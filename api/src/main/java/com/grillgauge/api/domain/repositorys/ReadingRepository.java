package com.grillgauge.api.domain.repositorys;

import com.grillgauge.api.domain.entitys.Reading;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Repository interface for managing Reading entities. */
@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

  List<Reading> findByProbeId(Long probeId);

  Optional<Reading> findTopByProbeIdOrderByTimeStampDesc(Long probeId);

  Long deleteAllByProbeId(Long probeId);

  List<Reading> findByProbe_IdAndTimeStampBetweenOrderByTimeStampAsc(
      Long probeId, Instant start, Instant end);

  @Modifying
  @Transactional
  @Query(
      value =
          "DELETE FROM reading WHERE id IN (SELECT id FROM reading WHERE expires_at < :cutoff ORDER"
              + " BY expires_at LIMIT :batchSize)",
      nativeQuery = true)
  int deleteExpiredBatch(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);
}
