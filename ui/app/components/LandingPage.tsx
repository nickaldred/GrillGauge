"use client";
import React, { useEffect } from "react";
import { useRouter } from "next/navigation";
import {
  ThermometerIcon,
  FlameIcon,
  CheckIcon,
  ArrowRightIcon,
} from "lucide-react";
import { useTheme } from "../providers/ThemeProvider";
import { useSession } from "next-auth/react";

export default function LandingPage() {
  // ** Theme **
  const { theme, toggle } = useTheme();
  const isDarkMode = theme === "dark";

  // ** Session & Router **
  const { status } = useSession();
  const router = useRouter();

  // Redirect to dashboard if authenticated
  useEffect(() => {
    if (status === "authenticated") {
      router.replace("/dashboard");
    }
  }, [status, router]);

  if (status === "authenticated") {
    return null;
  }

  // ** Features List **
  const features = [
    {
      title: "Multi-Probe Monitoring",
      description: "Track up to 8 probes simultaneously across multiple hubs",
      icon: ThermometerIcon,
    },
    {
      title: "Real-Time Alerts",
      description:
        "Get instant notifications when your food reaches target temperature",
      icon: FlameIcon,
    },
    {
      title: "Temperature History",
      description:
        "View detailed graphs and cooking patterns for every session",
      icon: CheckIcon,
    },
  ];

  return (
    <div
      className={`min-h-screen transition-colors duration-500 ${
        isDarkMode ? "bg-gray-900" : "bg-gray-50"
      }`}
    >
      <nav
        className={`fixed top-0 left-0 right-0 z-50 backdrop-blur-md ${
          isDarkMode
            ? "bg-gray-900/80 border-gray-800"
            : "bg-white/80 border-gray-200"
        } border-b transition-colors duration-500`}
      >
        <div className="container mx-auto px-6 py-4">
          <div className="flex justify-between items-center">
            <div className="flex items-center space-x-2">
              <div
                className={`p-2 rounded-lg ${
                  isDarkMode
                    ? "bg-gradient-to-br from-orange-500 to-red-600"
                    : "bg-gradient-to-br from-orange-400 to-red-500"
                }`}
              >
                <FlameIcon size={24} className="text-white" />
              </div>
              <span
                className={`text-xl font-bold ${
                  isDarkMode ? "text-white" : "text-gray-900"
                }`}
              >
                Grill Gauge
              </span>
            </div>
            <div className="flex items-center space-x-4">
              <button
                onClick={toggle}
                className={`p-2 rounded-lg transition-colors ${
                  isDarkMode
                    ? "bg-gray-800 text-yellow-400 hover:bg-gray-700 cursor-pointer"
                    : "bg-gray-200 text-gray-700 hover:bg-gray-300 cursor-pointer"
                }`}
                aria-label="Toggle theme"
              >
                {isDarkMode ? (
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="feather feather-sun"
                  >
                    <circle cx="12" cy="12" r="5"></circle>
                    <line x1="12" y1="1" x2="12" y2="3"></line>
                    <line x1="12" y1="21" x2="12" y2="23"></line>
                    <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
                    <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
                    <line x1="1" y1="12" x2="3" y2="12"></line>
                    <line x1="21" y1="12" x2="23" y2="12"></line>
                    <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
                    <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
                  </svg>
                ) : (
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    className="feather feather-moon"
                  >
                    <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
                  </svg>
                )}
              </button>
              <button
                onClick={() => router.push("/login")}
                className={`px-6 py-2 rounded-lg font-medium transition-all ${
                  isDarkMode
                    ? "bg-gradient-to-r from-orange-500 to-red-600 text-white hover:from-orange-600 hover:to-red-700 cursor-pointer"
                    : "bg-gradient-to-r from-orange-400 to-red-500 text-white hover:from-orange-500 hover:to-red-600 cursor-pointer"
                }`}
              >
                Sign In
              </button>
            </div>
          </div>
        </div>
      </nav>

      <section className="relative pt-32 pb-20 overflow-hidden">
        <div className="absolute inset-0 overflow-hidden">
          {isDarkMode ? (
            <>
              <div className="absolute top-0 left-1/4 w-96 h-96 bg-orange-500/20 rounded-full blur-3xl animate-pulse" />
              <div
                className="absolute bottom-0 right-1/4 w-96 h-96 bg-red-600/20 rounded-full blur-3xl animate-pulse"
                style={{ animationDelay: "1s" }}
              />
              <div
                className="absolute top-1/2 left-1/2 w-96 h-96 bg-yellow-500/10 rounded-full blur-3xl animate-pulse"
                style={{ animationDelay: "2s" }}
              />
            </>
          ) : (
            <>
              <div className="absolute top-0 left-1/4 w-96 h-96 bg-orange-200/40 rounded-full blur-3xl" />
              <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-red-200/40 rounded-full blur-3xl" />
            </>
          )}
        </div>

        <div className="container mx-auto px-6 relative z-10">
          <div className="max-w-4xl mx-auto text-center">
            <div className="inline-flex items-center space-x-2 px-4 py-2 rounded-full bg-gradient-to-r from-orange-500/10 to-red-600/10 border border-orange-500/20 mb-8">
              <FlameIcon size={16} className="text-orange-500" />
              <span
                className={`text-sm font-medium ${
                  isDarkMode ? "text-orange-400" : "text-orange-600"
                }`}
              >
                The Future of Grilling
              </span>
            </div>
            <h1
              className={`text-5xl md:text-7xl font-bold mb-6 ${
                isDarkMode ? "text-white" : "text-gray-900"
              }`}
            >
              Master the Art of
              <span className="block bg-gradient-to-r from-orange-500 via-red-500 to-red-600 bg-clip-text text-transparent">
                Perfect Temperature
              </span>
            </h1>
            <p
              className={`text-xl md:text-2xl mb-12 ${
                isDarkMode ? "text-gray-300" : "text-gray-600"
              } max-w-2xl mx-auto`}
            >
              Monitor multiple probes in real-time. Never overcook or undercook
              again. Achieve restaurant-quality results every time.
            </p>
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={() => router.push("/login")}
                className="group px-8 py-4 rounded-lg font-semibold text-lg bg-gradient-to-r from-orange-500 to-red-600 text-white hover cursor-pointer:from-orange-600 hover:to-red-700 transition-all shadow-lg hover:shadow-xl flex items-center justify-center cursor-pointer"
              >
                Get Started
                <ArrowRightIcon
                  size={20}
                  className="ml-2 group-hover:translate-x-1 transition-transform"
                />
              </button>
              <button
                className={`px-8 py-4 rounded-lg font-semibold text-lg transition-all cursor-pointer ${
                  isDarkMode
                    ? "bg-gray-800 text-white hover:bg-gray-700 "
                    : "bg-white text-gray-900 hover:bg-gray-100 border border-gray-300"
                }`}
              >
                Watch Demo
              </button>
            </div>
          </div>
        </div>
      </section>

      <section
        className={`py-20 ${
          isDarkMode ? "bg-gray-800/50" : "bg-white"
        } transition-colors duration-500`}
      >
        <div className="container mx-auto px-6">
          <div className="text-center mb-16">
            <h2
              className={`text-4xl font-bold mb-4 ${
                isDarkMode ? "text-white" : "text-gray-900"
              }`}
            >
              Why Grill Masters Choose Grill Gauge
            </h2>
            <p
              className={`text-xl ${
                isDarkMode ? "text-gray-400" : "text-gray-600"
              }`}
            >
              Professional-grade temperature monitoring for perfect results
            </p>
          </div>
          <div className="grid md:grid-cols-3 gap-8">
            {features.map((feature) => (
              <div
                key={feature.title}
                className={`p-8 rounded-2xl transition-all hover:scale-105 ${
                  isDarkMode
                    ? "bg-gray-900 border border-gray-800 hover:border-orange-500/50"
                    : "bg-gray-50 border border-gray-200 hover:border-orange-400"
                }`}
              >
                <div
                  className={`w-14 h-14 rounded-xl flex items-center justify-center mb-6 ${
                    isDarkMode
                      ? "bg-gradient-to-br from-orange-500 to-red-600"
                      : "bg-gradient-to-br from-orange-400 to-red-500"
                  }`}
                >
                  <feature.icon size={28} className="text-white" />
                </div>
                <h3
                  className={`text-2xl font-bold mb-3 ${
                    isDarkMode ? "text-white" : "text-gray-900"
                  }`}
                >
                  {feature.title}
                </h3>
                <p
                  className={`${
                    isDarkMode ? "text-gray-400" : "text-gray-600"
                  }`}
                >
                  {feature.description}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="py-20">
        <div className="container mx-auto px-6">
          <div className="grid md:grid-cols-4 gap-8">
            {[
              { value: "1+", label: "Active Users" },
              { value: "1+", label: "Perfect Cooks" },
              { value: "4.99★", label: "User Rating" },
              { value: "24/7", label: "Support" },
            ].map((stat) => (
              <div key={stat.label} className="text-center">
                <div
                  className={`text-4xl font-bold mb-2 bg-gradient-to-r from-orange-500 to-red-600 bg-clip-text text-transparent`}
                >
                  {stat.value}
                </div>
                <div
                  className={`text-lg ${
                    isDarkMode ? "text-gray-400" : "text-gray-600"
                  }`}
                >
                  {stat.label}
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section
        className={`py-20 ${
          isDarkMode
            ? "bg-gradient-to-br from-gray-900 via-gray-800 to-gray-900"
            : "bg-gradient-to-br from-orange-50 to-red-50"
        } transition-colors duration-500`}
      >
        <div className="container mx-auto px-6">
          <div className="max-w-4xl mx-auto text-center">
            <h2
              className={`text-4xl md:text-5xl font-bold mb-6 ${
                isDarkMode ? "text-white" : "text-gray-900"
              }`}
            >
              Ready to Elevate Your Grilling?
            </h2>
            <p
              className={`text-xl mb-10 ${
                isDarkMode ? "text-gray-300" : "text-gray-600"
              }`}
            >
              Join a single pitmaster achieving perfect results every time
            </p>
            <button
              onClick={() => router.push("/login")}
              className="group px-10 py-5 rounded-lg font-bold text-xl bg-gradient-to-r from-orange-500 to-red-600 text-white hover:from-orange-600 hover cursor-pointer:to-red-700 transition-all shadow-2xl hover:shadow-3xl flex items-center justify-center mx-auto cursor-pointer"
            >
              Start Cooking Better Today
              <ArrowRightIcon
                size={24}
                className="ml-2 group-hover:translate-x-1 transition-transform"
              />
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}
