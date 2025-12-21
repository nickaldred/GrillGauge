package com.grillgauge.api.utils;

import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import com.grillgauge.api.domain.repositorys.ReadingRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestUtils {

  @Autowired private UserRepository userRepository;

  @Autowired private HubRepository hubRepository;

  @Autowired private ProbeRepository probeRepository;

  @Autowired private ReadingRepository readingRepository;

  public void clearDatabase() {
    readingRepository.deleteAll();
    probeRepository.deleteAll();
    hubRepository.deleteAll();
    userRepository.deleteAll();
  }
}
