export type Probe = {
  id: number
  name: string
  currentTemp: number
  targetTemp: number
  color: string
  connected: boolean
}
export type Hub = {
  id: number
  name: string
  connected: boolean
  probes: Probe[]
}