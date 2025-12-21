package com.grillgauge.api.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Model representing a Probe's reading.
 *
 * <p>Contains the Probe's ID and its current temperature.
 */
@Getter
@Setter
@AllArgsConstructor
public class ProbeReading {
  private Integer id;
  private Float currentTemp;
}
