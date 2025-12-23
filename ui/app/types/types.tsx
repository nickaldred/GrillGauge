// The types used throughout the application.

/**
 * Represents a temperature probe connected to a hub.
 */
export type Probe = {
  id: number;
  localId: number;
  targetTemp: number;
  currentTemp: number;
  name: string;
  colour: string;
  connected: boolean;
};

/**
 * Represents a user of the application.
 *
 * Note: In the backend, the User entity uses `email` as the
 * primary key identifier (no separate numeric id field).
 */
export type User = {
  firstName: string;
  lastName: string;
  email: string;
};

/**
 * Represents a Hub device that connects probes to the application.
 */
export interface Hub {
  id: number;
  name: string;
  probes: Probe[];
  connected: boolean;
}

/**
 * Represents a temperature reading from a probe.
 */
export interface Reading {
  id: number;
  timestamp: string;
  temperature: number;
}
