import React, { ReactNode } from "react";
import "./globals.css";
import ThemeProvider from "./providers/ThemeProvider";
import { Header } from "./components/header";
import Footer from "./components/Footer";

type RootLayoutProps = {
  children: ReactNode;
};

export default function RootLayout({ children }: Readonly<RootLayoutProps>) {
  return (
    <html lang="en">
      <body className="min-h-screen flex flex-col">
        <ThemeProvider>
          <Header />
          <main className="flex-grow flex flex-col">{children}</main>
          <Footer />
        </ThemeProvider>
      </body>
    </html>
  );
}
