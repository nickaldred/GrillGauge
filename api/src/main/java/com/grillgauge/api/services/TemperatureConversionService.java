package com.grillgauge.api.services;

import com.grillgauge.api.domain.entitys.User;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Utility service for converting temperatures between Celsius and Fahrenheit. */
@Service
public class TemperatureConversionService {

  /** Convert Celsius to Fahrenheit. */
  public float toFahrenheit(final float celsius) {
    return roundToTwoDecimals((celsius * 9.0f / 5.0f) + 32.0f);
  }

  /** Convert Fahrenheit to Celsius. */
  public float toCelsius(final float fahrenheit) {
    return roundToTwoDecimals((fahrenheit - 32.0f) * 5.0f / 9.0f);
  }

  /**
   * Convert a stored Celsius value to the user's preferred unit.
   *
   * @param celsiusValue temperature stored in Celsius
   * @param unit preferred unit; defaults to Celsius when null
   * @return temperature expressed in the preferred unit, or null when input is null
   */
  public Float toUserUnit(final Float celsiusValue, final User.UserTemperatureUnit unit) {
    if (celsiusValue == null) {
      return null;
    }
    User.UserTemperatureUnit preferredUnit = resolveUnit(unit);
    float value =
        preferredUnit == User.UserTemperatureUnit.FAHRENHEIT
            ? toFahrenheit(celsiusValue)
            : celsiusValue;
    return roundToTwoDecimals(value);
  }

  /**
   * Convert a user-supplied value back to Celsius for storage.
   *
   * @param value the temperature provided by the user
   * @param unit the unit of the provided value; defaults to Celsius when null
   * @return temperature normalised to Celsius, or null when input is null
   */
  public Float toCelsiusFromUser(final Float value, final User.UserTemperatureUnit unit) {
    if (value == null) {
      return null;
    }
    User.UserTemperatureUnit providedUnit = resolveUnit(unit);
    float celsius = providedUnit == User.UserTemperatureUnit.FAHRENHEIT ? toCelsius(value) : value;
    return roundToTwoDecimals(celsius);
  }

  private float roundToTwoDecimals(final float value) {
    return Math.round(value * 100.0f) / 100.0f;
  }

  private User.UserTemperatureUnit resolveUnit(final User.UserTemperatureUnit unit) {
    return Optional.ofNullable(unit).orElse(User.UserTemperatureUnit.CELSIUS);
  }
}
