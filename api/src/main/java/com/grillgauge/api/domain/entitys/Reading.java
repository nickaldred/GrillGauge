package com.grillgauge.api.domain.entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a Reading taken from a Probe.
 *
 * <p>A Reading contains the current temperature and the timestamp when it was recorded.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Reading {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "probe_id", nullable = false)
  private Probe probe;

  private Instant timeStamp = Instant.now();

  @Column(nullable = false)
  private Float currentTemp;

  public Reading(final Probe probe, final Float currentTemp) {
    this.probe = probe;
    this.currentTemp = currentTemp;
  }

  /**
   * Constructor for Reading with specified timestamp.
   *
   * @param probe The Probe associated with this Reading.
   * @param currentTemp The current temperature recorded.
   * @param timeStamp The timestamp when the reading was taken.
   */
  public Reading(final Probe probe, final Float currentTemp, final Instant timeStamp) {
    this.probe = probe;
    this.currentTemp = currentTemp;
    this.timeStamp = timeStamp;
  }
}
