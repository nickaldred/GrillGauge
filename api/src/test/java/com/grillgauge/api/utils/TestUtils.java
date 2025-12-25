package com.grillgauge.api.utils;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import com.grillgauge.api.domain.repositorys.ReadingRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

  @Autowired private UserRepository userRepository;

  @Autowired private HubRepository hubRepository;

  @Autowired private ProbeRepository probeRepository;

  @Autowired private ReadingRepository readingRepository;

  /** Clears all data from the database repositories. */
  public void clearDatabase() {
    readingRepository.deleteAll();
    probeRepository.deleteAll();
    hubRepository.deleteAll();
    userRepository.deleteAll();
  }

  /**
   * Creates a JWT request post processor with the given subject and role.
   *
   * @param subject The subject of the JWT.
   * @param role The role to assign to the JWT.
   * @return A JwtRequestPostProcessor with the given subject and role.
   */
  @NonNull public static JwtRequestPostProcessor jwtWithRole(
      final @NonNull String subject, final @NonNull String role) {
    JwtRequestPostProcessor jwtRequestPostProcessor =
        jwt().jwt(jwt -> jwt.subject(subject)).authorities(new SimpleGrantedAuthority(role));
    if (jwtRequestPostProcessor == null) {
      throw new IllegalStateException("jwtRequestPostProcessor is null");
    }
    return jwtRequestPostProcessor;
  }
}
