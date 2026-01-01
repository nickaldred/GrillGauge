"use client";

type DemoHubBannerProps = {
  enabled: boolean;
  isDarkMode: boolean;
};

/**
 * Displays a banner indicating that the demo hub is enabled.
 *
 * @param enabled - Whether the demo hub is enabled.
 * @param isDarkMode - Whether the app is in dark mode.
 * @returns A React component rendering the banner or null.
 */
export function DemoHubBanner({ enabled, isDarkMode }: DemoHubBannerProps) {
  if (!enabled) return null;

  return (
    <div
      className={`mb-6 rounded-lg border-l-4 px-4 py-3 ${
        isDarkMode
          ? "bg-yellow-900/40 border-yellow-500 text-yellow-100"
          : "bg-yellow-50 border-yellow-400 text-yellow-800"
      }`}
      role="alert"
    >
      <p className="font-semibold">Demo hub is enabled</p>
      <p className="text-sm">
        Data from the demo hub is simulated mock data only and does not
        represent a real physical hub.
      </p>
    </div>
  );
}
