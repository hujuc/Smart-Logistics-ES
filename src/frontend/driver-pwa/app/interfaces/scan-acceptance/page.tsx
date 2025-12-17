"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import QrScanner from "@/components/qr-scanner";
import { ArrowLeft, CheckCircle, AlertCircle } from "lucide-react";
import Link from "next/link";
import { getAccessToken } from "../../../../shared/lib/auth";

export default function ScanPage() {
    const router = useRouter();
    const [scanResult, setScanResult] = useState<string | null>(null);
    const [status, setStatus] = useState<"idle" | "loading" | "success" | "error">("idle");
    const [message, setMessage] = useState<string>("");



    const handleScanSuccess = async (decodedText: string) => {
        // Prevent multiple scans while processing
        if (status === "loading" || status === "success") return;

        setScanResult(decodedText);
        setStatus("loading");
        setMessage("Processing scan...");

        try {
            let orderId = decodedText;

            // Robust logic to extract ID from URL or string
            // Handles:
            // 1. Just the ID: "123"
            // 2. Full URL: "http://.../orders/123/confirm" or "http://.../orders/123"
            if (decodedText.includes("/")) {
                // Remove trailing slash if present
                const cleanText = decodedText.endsWith("/") ? decodedText.slice(0, -1) : decodedText;
                const parts = cleanText.split("/");

                // Try to find the numeric part
                // Iterate from end to start
                for (let i = parts.length - 1; i >= 0; i--) {
                    if (!isNaN(Number(parts[i])) && parts[i].trim() !== "") {
                        orderId = parts[i];
                        break;
                    }
                }
            }

            console.log(`Extracted Order ID: ${orderId} from ${decodedText}`);

            const token = getAccessToken();

            // Call the backend API
            const response = await fetch(`/api/driver/orders/${orderId}/confirm`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
            });

            if (response.ok) {
                const data = await response.json();
                setStatus("success");
                setMessage(`Order #${orderId} confirmed successfully!`);

                // Optional: Redirect after a delay or let user scan another
                setTimeout(() => {
                    // Reset for next scan if desired, or redirect
                    // setStatus("idle");
                    // setScanResult(null);
                }, 3000);
            } else {
                const errorData = await response.json().catch(() => ({}));
                setStatus("error");
                setMessage(errorData.message || `Failed to confirm order (${response.status}). Please try again.`);
            }
        } catch (error) {
            console.error("Scan processing error:", error);
            setStatus("error");
            setMessage("An error occurred while processing the scan.");
        }
    };

    const handleReset = () => {
        setStatus("idle");
        setScanResult(null);
        setMessage("");
    };

    return (
        <div className="min-h-screen bg-slate-50 flex flex-col">
            {/* Header */}
            <header className="bg-white shadow-sm p-4 flex items-center sticky top-0 z-10">
                <Link href="/" className="p-2 mr-2 text-slate-600 hover:bg-slate-100 rounded-full">
                    <ArrowLeft size={24} />
                </Link>
                <h1 className="text-xl font-bold text-slate-800">Scan Package</h1>
            </header>

            {/* Content */}
            <main className="flex-1 p-4 flex flex-col items-center justify-center">

                {status === "idle" && (
                    <div className="w-full max-w-md space-y-4">
                        <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200 text-center mb-4">
                            <p className="text-slate-600">
                                Point your camera at the package QR code to confirm pickup/delivery.
                            </p>
                        </div>

                        <QrScanner
                            onScanSuccess={handleScanSuccess}
                            fps={10}
                            qrbox={250}
                            disableFlip={false}
                        />
                    </div>
                )}

                {status === "loading" && (
                    <div className="flex flex-col items-center justify-center space-y-4 p-8 bg-white rounded-xl shadow-md">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                        <p className="text-lg font-medium text-slate-700">Processing...</p>
                        <p className="text-sm text-slate-500">ID: {scanResult}</p>
                    </div>
                )}

                {status === "success" && (
                    <div className="flex flex-col items-center justify-center space-y-6 p-8 bg-white rounded-xl shadow-md w-full max-w-md">
                        <div className="rounded-full bg-green-100 p-4">
                            <CheckCircle size={48} className="text-green-600" />
                        </div>
                        <div className="text-center">
                            <h2 className="text-2xl font-bold text-slate-800 mb-2">Success!</h2>
                            <p className="text-slate-600">{message}</p>
                        </div>
                        <button
                            onClick={handleReset}
                            className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
                        >
                            Scan Another
                        </button>
                    </div>
                )}

                {status === "error" && (
                    <div className="flex flex-col items-center justify-center space-y-6 p-8 bg-white rounded-xl shadow-md w-full max-w-md">
                        <div className="rounded-full bg-red-100 p-4">
                            <AlertCircle size={48} className="text-red-600" />
                        </div>
                        <div className="text-center">
                            <h2 className="text-2xl font-bold text-slate-800 mb-2">Error</h2>
                            <p className="text-slate-600">{message}</p>
                        </div>
                        <button
                            onClick={handleReset}
                            className="w-full py-3 bg-slate-800 hover:bg-slate-900 text-white font-medium rounded-lg transition-colors"
                        >
                            Try Again
                        </button>
                    </div>
                )}
            </main>
        </div>
    );
}
