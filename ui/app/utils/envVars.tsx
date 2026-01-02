export const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;
export const API_BASE_URL = process.env.API_BASE_URL;
export const DEV_WARNING_ENABLED =
  (process.env.DEV_WARNING_ENABLED ?? "true").toLowerCase() === "true";
