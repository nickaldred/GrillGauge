"use client";

import { HubManagement } from "./components/HubManagement";
import { Header } from "./components/SetttingsHeader";

export default function Settings() {
  return (
    <div className="container mx-auto px-4 py-4">
      <Header />
      <HubManagement />;
    </div>
  );
}
