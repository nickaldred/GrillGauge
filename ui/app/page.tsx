"use client";

import { useEffect, useState } from "react";
import { Header } from "./components/header";
import { ProbeCard } from "./components/probeCard";
import { Probe } from "./types/types";

export default function Home() {
  const [user, setUser] = useState(null);

  const testProbe = {
    id: 1,
    name: "Probe 1",
    currentTemp: 55,
    targetTemp: 165,
    color: "red",
    connected: true,
  };

  async function getData(url: string) {
    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch data");
    }

    const data = await response.json();
    return data;
  }

  useEffect(() => {
    getData("http://localhost:8080/api/v1/user?email=swathi@gmail.com")
      .then((data) => {
        setUser(data);
      })
      .catch((error) => {
        console.error("Error fetching user:", error);
      });
  }, []);

  const handleUpdateTargetTemp = async (probeId: number, temp: number) => {};
  const handleUpdateName = async (probeId: number, name: string) => {};
  const onClick = async () => {};

  return (
    <main className="p-6">
      <Header />
      <ProbeCard
        probe={testProbe}
        hubName="hub1"
        onUpdateTargetTemp={handleUpdateTargetTemp}
        onUpdateName={handleUpdateName}
        onClick={onClick}
      />
      <p>Welcome to the Meat Thermometer Dashboard!</p>
      <p>User is: {user ? JSON.stringify(user) : "Loading user..."}</p>
    </main>
  );
}
