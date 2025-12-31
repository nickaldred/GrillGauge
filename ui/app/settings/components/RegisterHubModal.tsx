"use client";

import React, { useState, useRef } from "react";
import { useSession } from "next-auth/react";
import Modal from "@/app/components/modal";
import { useTheme } from "@/app/providers/ThemeProvider";
import { getData, postRequest } from "@/app/utils/requestUtils";
import { BASE_URL } from "@/app/utils/envVars";

type Props = {
  open: boolean;
  onClose: () => void;
  onRegistered?: () => void;
};

export default function RegisterHubModal({
  open,
  onClose,
  onRegistered,
}: Props) {
  // ** User Session **
  const { data: session } = useSession();
  const userEmail = session?.user?.email || "";

  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  type ButtonProps = {
    children: React.ReactNode;
    onClick?: React.MouseEventHandler<HTMLButtonElement> | (() => void);
    variant?: "primary" | "secondary";
    disabled?: boolean;
  };

  const Button = ({
    children,
    onClick,
    variant = "primary",
    disabled = false,
  }: ButtonProps) => {
    const base = "px-3 py-1 rounded focus:outline-none";
    const primary = `bg-blue-600 text-white ${
      disabled
        ? "opacity-50 cursor-not-allowed"
        : "hover:bg-blue-500 cursor-pointer"
    }`;
    const secondary = isDarkMode
      ? "border text-gray-300 hover:bg-gray-700 cursor-pointer"
      : "border text-gray-700 hover:bg-gray-100 cursor-pointer";
    const classes = `${base} ${variant === "primary" ? primary : secondary}`;

    const handleClick: React.MouseEventHandler<HTMLButtonElement> = (event) => {
      if (!onClick) return;

      if (onClick.length === 0) {
        (onClick as () => void)();
      } else {
        (onClick as React.MouseEventHandler<HTMLButtonElement>)(event);
      }
    };

    return (
      <button onClick={handleClick} disabled={disabled} className={classes}>
        {children}
      </button>
    );
  };

  // ** State **
  const [step, setStep] = useState<number>(0);
  const [hubIdInput, setHubIdInput] = useState("");
  const [otpInput, setOtpInput] = useState("");
  const pollingRef = useRef<number | null>(null);

  const reset = () => {
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }
    setStep(0);
    setHubIdInput("");
    setOtpInput("");
  };

  const closeAndReset = () => {
    reset();
    onClose();
  };

  /**
   * Start polling for hub registration completion.
   *
   * @param hubId The Hub ID.
   */
  const startPollingForRegistration = (hubId: string) => {
    if (!hubId) return;
    if (pollingRef.current) return;
    pollingRef.current = window.setInterval(async () => {
      try {
        const token = session?.apiToken as string | undefined;
        const hub = await getData<{ status?: string }>(
          `${BASE_URL}/hub?hubId=${hubId}`,
          token
        );
        if (hub?.status === "REGISTERED") {
          if (pollingRef.current) {
            clearInterval(pollingRef.current);
            pollingRef.current = null;
          }
          setStep(2);
          if (onRegistered) onRegistered();
        }
      } catch (err) {
        console.error("Polling error", err);
      }
    }, 3000);
  };

  /**
   * Handle confirming the hub registration.
   */
  const handleConfirm = async () => {
    if (!hubIdInput) {
      alert("Enter the Hub ID shown on the device.");
      return;
    }
    try {
      const token = session?.apiToken as string | undefined;

      await postRequest(
        `${BASE_URL}/register/confirm`,
        {
          hubId: Number(hubIdInput),
          otp: otpInput,
          userId: userEmail,
        },
        token
      );
      setStep(1);
      startPollingForRegistration(hubIdInput);
    } catch (e) {
      console.error(e);
      alert("Failed to confirm hub: " + (e as Error).message);
    }
  };

  return (
    <Modal open={open} onClose={closeAndReset} title="Register New Hub">
      <div
        className={`space-y-4 ${
          isDarkMode ? "text-gray-300" : "text-gray-700"
        }`}
      >
        {step === 0 && (
          <div>
            <p className="text-sm mb-2">
              Place the hub into registration mode (refer to the device
              instructions) and enter the Hub ID and OTP displayed on the hub
              screen below.
            </p>
            <label className="block text-sm font-medium">Hub ID</label>
            <input
              value={hubIdInput}
              onChange={(e) => setHubIdInput(e.target.value)}
              className="w-full mt-1 p-2 border rounded"
              placeholder="Enter the Hub ID shown on the device"
            />

            <label className="block text-sm font-medium mt-3">OTP</label>
            <input
              value={otpInput}
              onChange={(e) => setOtpInput(e.target.value)}
              className="w-full mt-1 p-2 border rounded"
              placeholder="Enter the OTP shown on the device"
            />

            <div className="flex justify-end mt-4 space-x-2">
              <Button variant="secondary" onClick={closeAndReset}>
                Cancel
              </Button>
              <Button
                onClick={handleConfirm}
                disabled={!hubIdInput || !otpInput}
              >
                Confirm
              </Button>
            </div>
          </div>
        )}

        {step === 1 && (
          <div>
            <p className="text-sm mb-2">Waiting for the Hub to pair.</p>
            <p className="text-sm text-gray-500 mb-4">
              The Hub should pair automatically once it has your OTP and network
              access. The UI will poll the server and update when the Hub
              becomes registered.
            </p>

            <div className="flex items-center space-x-3">
              <div className="w-4 h-4 rounded-full bg-blue-600 animate-pulse" />
              <div className="text-sm">Waiting for Hub...</div>
            </div>

            <div className="flex justify-between mt-4">
              <Button variant="secondary" onClick={() => setStep(0)}>
                Back
              </Button>
              <div>
                <Button variant="secondary" onClick={closeAndReset}>
                  Cancel
                </Button>
              </div>
            </div>
          </div>
        )}

        {step === 2 && (
          <div>
            <p className="text-sm mb-2">Hub successfully paired.</p>
            <p className="text-sm text-gray-500 mb-4">
              The Hub has been successfully paired with your account. You can
              close this dialog.
            </p>

            <div className="flex justify-end mt-3">
              <Button variant="secondary" onClick={closeAndReset}>
                Done
              </Button>
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
}
