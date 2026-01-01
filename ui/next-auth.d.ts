import type { DefaultSession } from "next-auth";
import "next-auth";

import type { User as GrillUser, TemperatureUnit } from "./app/types/types";

declare module "next-auth" {
  interface Session {
    user?: DefaultSession["user"] &
      Pick<GrillUser, "firstName" | "lastName" | "roles"> & {
        temperatureUnit?: TemperatureUnit;
      };
    apiToken?: string;
  }
}

export {};
