package com.grillgauge.api.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model representing a Probe for front-end consumption.
 *
 * <p>Contains the Probe's ID, local ID, target temperature, current temperature, name, colour, and
 * connection status.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FrontEndProbe {

  private Long id;
  private Integer localId;
  private Float targetTemp;
  private Float currentTemp;
  private String name;
  private String colour;
  private Boolean connected;
}
