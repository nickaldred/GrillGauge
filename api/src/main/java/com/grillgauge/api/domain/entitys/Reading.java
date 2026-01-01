package com.grillgauge.api.domain.entitys;

import com.grillgauge.api.domain.entitys.User.UserReadingExpiry;
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
  private Instant expiresAt;

  @Column(nullable = false)
  private Float currentTemp;

  /**
   * Constructor for Reading with current timestamp.
   *
   * @param probe The Probe associated with this Reading.
   * @param currentTemp The current temperature recorded.
   * @param expiresIn The user's reading expiry setting.
   */
  public Reading(final Probe probe, final Float currentTemp, final UserReadingExpiry expiresIn) {
    this.probe = probe;
    this.currentTemp = currentTemp;
    this.expiresAt = calculateExpiry(this.timeStamp, expiresIn);
  }

  /**
   * Constructor for Reading with specified timestamp.
   *
   * @param probe The Probe associated with this Reading.
   * @param currentTemp The current temperature recorded.
   * @param timeStamp The timestamp when the reading was taken.
   * @param expiresIn The user's reading expiry setting.
   */
  public Reading(
      final Probe probe,
      final Float currentTemp,
      final Instant timeStamp,
      final UserReadingExpiry expiresIn) {
    this.probe = probe;
    this.currentTemp = currentTemp;
    this.timeStamp = timeStamp;
    this.expiresAt = calculateExpiry(timeStamp, expiresIn);
  }

  /* Calculate the expiry time based on the reading timestamp and user's expiry setting. */
  private Instant calculateExpiry(final Instant timeStamp, final UserReadingExpiry expiresIn) {
    return switch (expiresIn) {
      case ONE_HOUR -> timeStamp.plusSeconds(3600);
      case SIX_HOURS -> timeStamp.plusSeconds(21600);
      case TWELVE_HOURS -> timeStamp.plusSeconds(43200);
      case ONE_DAY -> timeStamp.plusSeconds(86400);
      case THREE_DAYS -> timeStamp.plusSeconds(259200);
      case ONE_WEEK -> timeStamp.plusSeconds(604800);
      case TWO_WEEKS -> timeStamp.plusSeconds(1209600);
      case ONE_MONTH -> timeStamp.plusSeconds(2592000);
    };
  }
}
