import type { TemperatureUnit } from "../types/types";

// ** Default Temperature Unit **
export const defaultTemperatureUnit: TemperatureUnit = "CELSIUS";

/**
 * Get the symbol for a temperature unit.
 * 
 * @param unit - The temperature unit - CELSIUS or FAHRENHEIT.
 * @returns The symbol for the temperature unit.
 */
export const temperatureUnitSymbol = (unit?: TemperatureUnit) =>
  unit === "FAHRENHEIT" ? "F" : "C";
