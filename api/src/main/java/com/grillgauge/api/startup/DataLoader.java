package com.grillgauge.api.startup;

import static java.util.Objects.requireNonNull;

import com.grillgauge.api.domain.entitys.Hub;
import com.grillgauge.api.domain.entitys.Probe;
import com.grillgauge.api.domain.entitys.Reading;
import com.grillgauge.api.domain.entitys.User;
import com.grillgauge.api.domain.repositorys.HubRepository;
import com.grillgauge.api.domain.repositorys.ProbeRepository;
import com.grillgauge.api.domain.repositorys.ReadingRepository;
import com.grillgauge.api.domain.repositorys.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * DataLoader is a component that implements CommandLineRunner to load initial data into the
 * database when the application starts. FOR TESTING PURPOSES ONLY. It creates and saves sample
 * users, hubs, probes, and readings to their respective repositories.
 */
@Component
@Profile("dev")
public class DataLoader implements CommandLineRunner {
  private UserRepository userRepository;
  private HubRepository hubRepository;
  private ProbeRepository probeRepository;
  private ReadingRepository readingRepository;

  /**
   * Constructor for DataLoader.
   *
   * @param userRepository The UserRepository to use for loading user data.
   * @param hubRepository The HubRepository to use for loading hub data.
   * @param probeRepository The ProbeRepository to use for loading probe data.
   * @param readingRepository The ReadingRepository to use for loading reading data.
   */
  public DataLoader(
      UserRepository userRepository,
      HubRepository hubRepository,
      ProbeRepository probeRepository,
      ReadingRepository readingRepository) {
    this.userRepository = userRepository;
    this.hubRepository = hubRepository;
    this.probeRepository = probeRepository;
    this.readingRepository = readingRepository;
  }

  @Override
  public void run(String... args) throws Exception {
    dataLoader();
  }

  private void dataLoader() {
    User testUser1 = new User("froglet88@gmail.com", "nick", "aldred");
    User testUser2 = new User("nickaldred1994@gmail.com", "joe", "bloggs");
    User testUser3 = new User("swathi@gmail.com", "swathi", "guptha");
    User testUser4 = new User("tom@askjeeves.com", "tom", "jones");
    userRepository.saveAll(requireNonNull(List.of(testUser1, testUser2, testUser3, testUser4)));

    Hub testHub1 = new Hub(testUser1, "hub1");
    Hub testHub3 = new Hub(testUser3, "hub3");
    Hub testHub4 = new Hub(testUser4, "hub4");
    Hub testHub5 = new Hub(testUser1, "hub5");
    hubRepository.saveAll(requireNonNull(List.of(testHub1, testHub3, testHub4, testHub5)));

    Probe testProbe1 = new Probe(1, testHub1, testUser1, (float) 180.00, "probe1");
    Probe testProbe2 = new Probe(2, testHub1, testUser1, (float) 200.00, "probe2");
    Probe testProbe3 = new Probe(3, testHub1, testUser1, (float) 160.00, "probe3");
    Probe testProbe6 = new Probe(1, testHub3, testUser3, (float) 190.00, "probe6");
    Probe testProbe7 = new Probe(1, testHub4, testUser4, (float) 210.00, "probe7");
    Probe testProbe8 = new Probe(1, testHub5, testUser1, (float) 220.00, "probe8");
    probeRepository.saveAll(
        requireNonNull(
            List.of(testProbe1, testProbe2, testProbe3, testProbe6, testProbe7, testProbe8)));

    Reading testReading1 = new Reading(testProbe1, (float) 135, Instant.now().minusSeconds(300));
    Reading testReading2 = new Reading(testProbe1, (float) 145, Instant.now().minusSeconds(200));
    Reading testReading3 = new Reading(testProbe1, (float) 165, Instant.now().minusSeconds(100));
    Reading testReading4 = new Reading(testProbe1, (float) 175, Instant.now());
    Reading testReading5 = new Reading(testProbe2, (float) 135, Instant.now().minusSeconds(300));
    Reading testReading6 = new Reading(testProbe2, (float) 135, Instant.now().minusSeconds(200));
    Reading testReading7 = new Reading(testProbe2, (float) 140, Instant.now().minusSeconds(100));
    Reading testReading8 = new Reading(testProbe2, (float) 150, Instant.now());
    Reading testReading9 = new Reading(testProbe3, (float) 125, Instant.now().minusSeconds(300));
    Reading testReading10 = new Reading(testProbe3, (float) 130, Instant.now().minusSeconds(200));
    Reading testReading11 = new Reading(testProbe3, (float) 135, Instant.now().minusSeconds(100));
    Reading testReading12 = new Reading(testProbe3, (float) 145, Instant.now());
    Reading testReading13 = new Reading(testProbe8, (float) 125, Instant.now().minusSeconds(300));
    Reading testReading14 = new Reading(testProbe8, (float) 135, Instant.now().minusSeconds(200));
    Reading testReading15 = new Reading(testProbe8, (float) 140, Instant.now().minusSeconds(200));
    Reading testReading16 = new Reading(testProbe8, (float) 145, Instant.now());

    readingRepository.saveAll(
        requireNonNull(
            List.of(
                testReading1,
                testReading2,
                testReading3,
                testReading4,
                testReading5,
                testReading6,
                testReading7,
                testReading8,
                testReading9,
                testReading10,
                testReading11,
                testReading12,
                testReading13,
                testReading14,
                testReading15,
                testReading16)));
  }
}
