package com.grillgauge.api.services;

import com.grillgauge.api.domain.models.FrontEndHub;
import com.grillgauge.api.domain.models.FrontEndProbe;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Provides a single shared demo hub with four demo probes whose temperatures change over time.
 * Every user sees the same simulated hub and probe readings.
 */
@Service
public class DemoService {

  private static final long DEMO_HUB_ID = -1L;
  private static final String DEMO_HUB_NAME = "Demo Hub";
  private static final int HISTORY_CAPACITY = 720; // 720 * 30s ≈ 6 hours of history

  private final Clock clock;

  private final Map<Long, ProbeState> probeStates = new ConcurrentHashMap<>();
  private final Map<Long, Deque<DemoReading>> probeHistories = new ConcurrentHashMap<>();

  public DemoService() {
    this(Clock.systemUTC());
  }

  /**
   * Constructor for injecting a custom clock (useful for testing).
   *
   * @param clock The clock to use.
   */
  private DemoService(Clock clock) {
    this.clock = Objects.requireNonNull(clock, "clock");
    initialiseProbes();
  }

  /** Returns the shared demo hub with the latest probe temperatures. */
  public FrontEndHub getDemoHub() {
    List<FrontEndProbe> probes = new ArrayList<>();
    probeStates.values().stream()
        .sorted((a, b) -> Integer.compare(a.localId, b.localId))
        .forEach(
            state ->
                probes.add(
                    new FrontEndProbe(
                        state.probeId,
                        state.localId,
                        state.targetTemp,
                        state.currentTemp,
                        state.name,
                        state.colour,
                        true,
                        true)));

    return new FrontEndHub(DEMO_HUB_ID, DEMO_HUB_NAME, probes, true, true);
  }

  /**
   * Get the historical readings for the given demo probe between the given timestamps.
   *
   * @param probeId The demo probe ID.
   * @param start The start timestamp.
   * @param end The end timestamp.
   * @return List of DemoReading records.
   */
  public List<DemoReading> getReadingsForProbeBetween(
      final long probeId, final Instant start, final Instant end) {
    Deque<DemoReading> history = probeHistories.get(probeId);
    if (history == null) {
      return List.of();
    }

    return history.stream()
        .filter(r -> !r.timestamp().isBefore(start) && !r.timestamp().isAfter(end))
        .sorted(Comparator.comparing(DemoReading::timestamp))
        .toList();
  }

  /** Advances the simulation by updating probe temperatures and recording their history. */
  @Scheduled(fixedRate = 30_000)
  void advanceSimulation() {
    Instant now = clock.instant();
    ThreadLocalRandom rnd = ThreadLocalRandom.current();

    for (ProbeState state : probeStates.values()) {
      state.phase += state.phaseIncrement;
      double oscillation = Math.sin(state.phase) * state.amplitude;
      double jitter = rnd.nextDouble(-1.5, 1.5);
      float nextTemp = (float) Math.round(state.baseTemp + oscillation + jitter);

      state.currentTemp = nextTemp;
      appendHistory(state.probeId, now, nextTemp);
    }
  }

  /** Initialises the demo probes with predefined settings. */
  private void initialiseProbes() {
    registerProbe(-101L, 1, "Demo Probe 1", "#ef4444", 165f, 100f, 18d, 0.0d);
    registerProbe(-102L, 2, "Demo Probe 2", "#f97316", 165f, 120f, 14d, Math.PI / 3);
    registerProbe(-103L, 3, "Demo Probe 3", "#22c55e", 135f, 90f, 12d, 2 * Math.PI / 3);
    registerProbe(-104L, 4, "Demo Probe 4", "#3b82f6", 180f, 100f, 16d, Math.PI);
  }

  /**
   * Registers a demo probe with the given parameters.
   *
   * @param probeId The demo probe ID.
   * @param localId The local ID of the probe.
   * @param name The name of the probe.
   * @param colour The colour associated with the probe.
   * @param targetTemp The target temperature for the probe.
   * @param baseTemp The base temperature for the probe.
   * @param amplitude The amplitude of temperature oscillation.
   * @param initialPhase The initial phase of the temperature oscillation.
   */
  private void registerProbe(
      long probeId,
      int localId,
      String name,
      String colour,
      float targetTemp,
      float baseTemp,
      double amplitude,
      double initialPhase) {
    ProbeState state =
        new ProbeState(probeId, localId, name, colour, targetTemp, baseTemp, amplitude);
    state.phase = initialPhase;
    probeStates.put(probeId, state);
    Deque<DemoReading> history = new ArrayDeque<>(HISTORY_CAPACITY);
    history.add(new DemoReading(clock.instant(), baseTemp));
    probeHistories.put(probeId, history);
  }

  /** Appends a new reading to the history of the given demo probe. */
  private void appendHistory(long probeId, Instant timestamp, float temperature) {
    Deque<DemoReading> history = probeHistories.get(probeId);
    if (history == null) {
      history = new ArrayDeque<>(HISTORY_CAPACITY);
      probeHistories.put(probeId, history);
    }
    history.addLast(new DemoReading(timestamp, temperature));
    while (history.size() > HISTORY_CAPACITY) {
      history.removeFirst();
    }
  }

  /** Simple in-memory state per demo probe. */
  private static final class ProbeState {
    final long probeId;
    final int localId;
    final String name;
    final String colour;
    final float targetTemp;
    final float baseTemp;
    final double amplitude;
    final double phaseIncrement;
    volatile float currentTemp;
    double phase;

    ProbeState(
        long probeId,
        int localId,
        String name,
        String colour,
        float targetTemp,
        float baseTemp,
        double amplitude) {
      this.probeId = probeId;
      this.localId = localId;
      this.name = name;
      this.colour = colour;
      this.targetTemp = targetTemp;
      this.baseTemp = baseTemp;
      this.amplitude = amplitude;
      this.phaseIncrement = (2 * Math.PI) / 18.0;
      this.phase = 0.0;
      this.currentTemp = baseTemp;
    }
  }

  /** Immutable reading for a demo probe. */
  public record DemoReading(Instant timestamp, float temperature) {}
}
