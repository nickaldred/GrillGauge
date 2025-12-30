package com.grillgauge.api.domain.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Model representing a Hub for front-end consumption. */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FrontEndHub {

  private Long id;
  private String name;
  private List<FrontEndProbe> probes;
  private boolean connected;
  private boolean visible;
}
