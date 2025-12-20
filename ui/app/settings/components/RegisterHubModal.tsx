"use client";

import React, { useState, useRef } from "react";
import { useSession } from "next-auth/react";
import Modal from "@/app/components/modal";
import { useTheme } from "@/app/providers/ThemeProvider";
import { postRequest } from "@/app/utils/requestUtils";
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

  // ** State **
  const [step, setStep] = useState<number>(0);
  const [hubIdInput, setHubIdInput] = useState("");
  const [otpInput, setOtpInput] = useState("");
  const [signedCertPem, setSignedCertPem] = useState<string | null>(null);
  const [polling, setPolling] = useState(false);
  const pollingRef = useRef<number | null>(null);

  const reset = () => {
    if (pollingRef.current) {
      clearInterval(pollingRef.current);
      pollingRef.current = null;
    }
    setPolling(false);
    setStep(0);
    setHubIdInput("");
    setOtpInput("");
    setSignedCertPem(null);
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
    setPolling(true);
    pollingRef.current = window.setInterval(async () => {
      try {
        const res = await fetch(`${BASE_URL}/hub?hubId=${hubId}`);
        if (!res.ok) throw new Error(`Failed to fetch hub: ${res.status}`);
        const hub = await res.json();
        if (hub?.status === "REGISTERED") {
          setSignedCertPem(hub.certificatePem || null);
          setPolling(false);
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
      await postRequest(`${BASE_URL}/register/confirm`, {
        hubId: Number(hubIdInput),
        otp: otpInput,
        userId: userEmail,
      });
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
              <button
                onClick={closeAndReset}
                className={`px-3 py-1 border rounded cursor-pointer ${
                  isDarkMode ? "hover:bg-gray-700" : "hover:bg-gray-100"
                }`}
              >
                Cancel
              </button>
              <button
                onClick={handleConfirm}
                className="px-3 py-1 bg-blue-600 text-white rounded cursor-pointer hover:bg-blue-700"
                disabled={!hubIdInput || !otpInput}
              >
                Confirm
              </button>
            </div>
          </div>
        )}

        {step === 1 && (
          <div>
            <p className="text-sm mb-2">
              Waiting for the Hub to pair and request a certificate.
            </p>
            <p className="text-sm text-gray-500 mb-4">
              The Hub should post its CSR automatically once it has your OTP and
              network access. The UI will poll the server and update when the
              Hub becomes registered.
            </p>

            <div className="flex items-center space-x-3">
              <div className="w-4 h-4 rounded-full bg-blue-600 animate-pulse" />
              <div className="text-sm">Waiting for Hub...</div>
            </div>

            <div className="flex justify-between mt-4">
              <button
                onClick={() => setStep(0)}
                className="px-3 py-1 border rounded"
              >
                Back
              </button>
              <div>
                <button
                  onClick={closeAndReset}
                  className="px-3 py-1 border rounded"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        )}

        {step === 2 && signedCertPem && (
          <div>
            <p className="text-sm mb-2">Signed certificate (PEM)</p>
            <textarea
              value={signedCertPem}
              readOnly
              rows={10}
              className="w-full p-2 border rounded"
            />
            <div className="flex justify-end mt-3 space-x-2">
              <button
                onClick={() => {
                  navigator.clipboard?.writeText(signedCertPem);
                }}
                className="px-3 py-1 border rounded"
              >
                Copy
              </button>
              <button
                onClick={() => {
                  const blob = new Blob([signedCertPem], {
                    type: "application/x-pem-file",
                  });
                  const url = URL.createObjectURL(blob);
                  const a = document.createElement("a");
                  a.href = url;
                  a.download = `hub-${hubIdInput}-cert.pem`;
                  a.click();
                  URL.revokeObjectURL(url);
                }}
                className="px-3 py-1 bg-green-600 text-white rounded"
              >
                Download
              </button>
              <button
                onClick={() => {
                  closeAndReset();
                }}
                className="px-3 py-1 bg-gray-600 text-white rounded"
              >
                Done
              </button>
            </div>
          </div>
        )}
      </div>
    </Modal>
  );
}
