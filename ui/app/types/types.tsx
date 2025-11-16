export type Probe = {
  id: number;
  localId: number;
  targetTemp: number;
  currentTemp: number;
  name: string;
  colour: string;
  connected: boolean;
};

export type User = {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
};

export interface Hub {
  id: number;
  name: string;
  probes: Probe[];
  connected: boolean;
}
