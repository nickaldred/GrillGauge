package com.grillgauge.api.domain.repositorys;

import org.springframework.stereotype.Repository;

import com.grillgauge.api.domain.entitys.User;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
