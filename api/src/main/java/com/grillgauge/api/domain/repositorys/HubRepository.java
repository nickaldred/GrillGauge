package com.grillgauge.api.domain.repositorys;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grillgauge.api.domain.entitys.Hub;

@Repository
public interface HubRepository extends JpaRepository<Hub, Long> {

    List<Hub> findByOwnerEmail(String email);
}
