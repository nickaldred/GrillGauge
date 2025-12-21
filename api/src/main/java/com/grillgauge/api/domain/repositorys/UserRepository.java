package com.grillgauge.api.domain.repositorys;

import com.grillgauge.api.domain.entitys.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository interface for User entities. */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> deleteByEmail(String email);

  Optional<User> findByEmail(String email);
}
