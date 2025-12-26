package com.grillgauge.api.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

/**
 * Model representing a Probe for front-end consumption.
 * Contains the Probe's ID, local ID, target temperature, current temperature, name, colour, and
 * connection status.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FrontEndProbe {

  @NonNull
  private Long id;
  @NonNull
  private Integer localId;
  private Float targetTemp;
  private Float currentTemp;
  @NonNull
  private String name;
  @NonNull
  private String colour;
  @NonNull
  private Boolean connected;
}
