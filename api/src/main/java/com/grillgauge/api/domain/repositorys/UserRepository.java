package com.grillgauge.api.domain.repositorys;

import org.springframework.stereotype.Repository;
import com.grillgauge.api.domain.entitys.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> deleteByEmail(String email);

    Optional<User> findByEmail(String email);
}
