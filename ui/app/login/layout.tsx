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

/**
 * The root layout component that wraps the login page.
 *
 * @param children The child components to render within the layout.
 * @returns The RootLayout component.
 */
export default function RootLayout({ children }: Readonly<RootLayoutProps>) {
  return (
    <html lang="en">
      <body className="min-h-screen flex flex-col">
        <ThemeProvider>
          <Header />
          <main className="flex-grow">{children}</main>
        </ThemeProvider>
      </body>
    </html>
  );
}
