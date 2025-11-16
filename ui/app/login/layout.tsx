import React, { ReactNode } from "react";
import "../globals.css";
import ThemeProvider from "../providers/ThemeProvider";
import { Header } from "../components/header";

type RootLayoutProps = {
  children: ReactNode;
};

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
