import React, { JSX } from "react";
import { LoadingState } from "../components/LoadingState";

/** Shared auth/loading shell to avoid layout flashes while auth resolves. */
export function renderAuthLoading(
  isDarkMode: boolean,
  message: string
): JSX.Element {
  return (
    <div
      className={`${
        isDarkMode ? "bg-gray-900" : "bg-gray-100"
      } w-full flex-1 flex flex-col p-6`}
    >
      <div className="container mx-auto px-4 py-4">
        <LoadingState message={message} />
      </div>
    </div>
  );
}
