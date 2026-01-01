package com.grillgauge.api.domain.entitys;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.NonNull;

/** Entity representing a User in the system. */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

  /** User roles within the system. */
  public enum UserRole {
    ADMIN,
    USER
  }

  /** User reading expiry options. */
  public enum UserReadingExpiry {
    ONE_HOUR,
    SIX_HOURS,
    TWELVE_HOURS,
    ONE_DAY,
    THREE_DAYS,
    ONE_WEEK,
    TWO_WEEKS,
    ONE_MONTH
  }

  /** User temperature unit preferences. */
  public enum UserTemperatureUnit {
    FAHRENHEIT,
    CELSIUS
  }

  @Id
  @Column(nullable = false, unique = true)
  @NonNull
  private String email;

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false)
  private List<UserRole> roles;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserReadingExpiry readingExpiry = UserReadingExpiry.ONE_WEEK;

  @Column(nullable = false)
  private Boolean demoHubEnabled = false;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserTemperatureUnit temperatureUnit = UserTemperatureUnit.CELSIUS;

  /**
   * Constructor for User with default role USER.
   *
   * @param email The email of the user.
   * @param firstName The first name of the user.
   * @param lastName The last name of the user.
   */
  public User(@NonNull String email, @NonNull String firstName, @NonNull String lastName) {
    this.email = email;
    this.firstName = firstName;
    this.lastName = lastName;
    this.roles = List.of(UserRole.USER);
  }

  public static UserTemperatureUnit resolveUnit(final User owner) {
    if (owner == null || owner.getTemperatureUnit() == null) {
      return User.UserTemperatureUnit.CELSIUS;
    }
    return owner.getTemperatureUnit();
  }
}
