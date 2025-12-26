package com.grillgauge.api.domain.entitys;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a Probe in the system. A Probe is associated with a Hub and a User (owner),
 * and can have multiple Readings.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
public class Probe {

  /** Default probe colours for the probe card in the dashboard. */
  private static final List<String> DEFAULT_COLOURS =
      List.of(
          "#E91E63", // pink
          "#9C27B0", // purple
          "#673AB7", // deep purple
          "#3F51B5", // indigo
          "#2196F3", // blue
          "#03A9F4", // light blue
          "#00BCD4", // cyan
          "#009688", // teal
          "#4CAF50", // green
          "#8BC34A", // light green
          "#CDDC39", // lime
          "#FFEB3B", // yellow
          "#FFC107", // amber
          "#FF9800", // orange
          "#FF5722" // deep orange
      );

  private static int nextColourIndex = 0;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(name = "local_id", nullable = false)
  private Integer localId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "hub_id", nullable = false)
  private Hub hub;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "email", nullable = false)
  private User owner;

  @Column(nullable = true)
  private Float targetTemp;

  @Column(nullable = true)
  private String name;

  @Column(nullable = false)
  private String colour;

  @OneToMany(mappedBy = "probe", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Reading> readings = new ArrayList<>();

  public Probe(Integer localId, Hub hub, User owner) {
    this(localId, hub, owner, null, null);
  }

  public Probe(Integer localId, Hub hub, User owner, Float targetTemp) {
    this(localId, hub, owner, targetTemp, null);
  }

  /**
   * Constructor for Probe with all parameters.
   *
   * @param localId The local ID of the Probe.
   * @param hub The Hub associated with the Probe.
   * @param owner The User who owns the Probe.
   * @param targetTemp The target temperature for the Probe.
   * @param name The name of the Probe.
   */
  public Probe(Integer localId, Hub hub, User owner, Float targetTemp, String name) {
    this.localId = localId;
    this.hub = hub;
    this.owner = owner;
    this.targetTemp = targetTemp;
    this.name = name;
  }

  @PrePersist
  private void assignDefaultColour() {
    if (this.colour == null || this.colour.isEmpty()) {
      this.colour =
          DEFAULT_COLOURS.stream()
              .skip(nextColourIndex % DEFAULT_COLOURS.size())
              .findFirst()
              .orElse("#2196F3");
      nextColourIndex++;
    }
  }
}
