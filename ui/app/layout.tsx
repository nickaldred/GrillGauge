import React, { ReactNode } from "react";
import { cookies } from "next/headers";
import "./globals.css";
import ThemeProvider from "./providers/ThemeProvider";
import { Header } from "./components/header";
import Footer from "./components/Footer";

type RootLayoutProps = {
  children: ReactNode;
};

export default async function RootLayout({
  children,
}: Readonly<RootLayoutProps>) {
  const cookieStore = await cookies();
  const themeCookie = cookieStore.get("theme")?.value;
  const initialTheme =
    themeCookie === "dark" || themeCookie === "light" ? themeCookie : "light";

  return (
    <html
      lang="en"
      data-theme={initialTheme}
      className={initialTheme === "dark" ? "dark" : undefined}
      suppressHydrationWarning
    >
      <body>
        <ThemeProvider initialTheme={initialTheme}>
          <div className="min-h-screen flex flex-col">
            <Header />
            <main className="flex-1 flex flex-col">{children}</main>
            <Footer />
          </div>
        </ThemeProvider>
      </body>
    </html>
  );
}
