import React, { ReactNode } from "react";
import "../globals.css";
import ThemeProvider from "../providers/ThemeProvider";
import { Header } from "../components/header";

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
