package com.grillgauge.api.domain.repositorys;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grillgauge.api.domain.entitys.Probe;

@Repository
public interface ProbeRepository extends JpaRepository<Probe, Long> {
}
