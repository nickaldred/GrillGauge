package com.grillgauge.api.domain.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grillgauge.api.domain.entitys.Reading;
import java.util.List;

@Repository
public interface ReadingRepository extends JpaRepository<Reading, Long> {

    List<Reading> findByProbeId(Long probeId);
}
