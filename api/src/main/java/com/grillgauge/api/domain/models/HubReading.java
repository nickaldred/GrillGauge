package com.grillgauge.api.domain.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Model representing a Hub's readings.
 *
 * <p>Contains the Hub's ID and a list of ProbeReadings.
 */
@Getter
@Setter
@AllArgsConstructor
public class HubReading {
  private Long id;
  private List<ProbeReading> probeReadings;
}
