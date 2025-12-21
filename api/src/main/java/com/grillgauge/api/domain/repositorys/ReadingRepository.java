package com.grillgauge.api.domain.repositorys;

import com.grillgauge.api.domain.entitys.Reading;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for managing Reading entities. */
@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

  List<Reading> findByProbeId(Long probeId);

  Optional<Reading> findTopByProbeIdOrderByTimeStampDesc(Long probeId);

  Long deleteAllByProbeId(Long probeId);

  List<Reading> findByProbe_IdAndTimeStampBetweenOrderByTimeStampAsc(
      Long probeId, Instant start, Instant end);
}
