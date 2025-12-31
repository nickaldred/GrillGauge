import React, { ReactNode } from "react";
import "../globals.css";

/**
 * Props for the RootLayout component.
 */
type RootLayoutProps = {
  children: ReactNode;
};

/** The root layout component. */
export default function RootLayout({ children }: Readonly<RootLayoutProps>) {
  return <>{children}</>;
}
