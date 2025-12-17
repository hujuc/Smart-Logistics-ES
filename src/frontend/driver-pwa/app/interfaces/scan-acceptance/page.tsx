"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import QrScanner from "@/components/qr-scanner";
import { ArrowLeft, CheckCircle, AlertCircle, Package } from "lucide-react";
import Link from "next/link";
import { getAccessToken } from "../../../../shared/lib/auth";

interface Order {
    id: number;
    reference: string;
    status: string;
}

interface Shipment {
    id: number;
    shipmentCode: string;
    totalOrders: number;
    status: string;
}

export default function ScanPage() {
    const router = useRouter();
    const [scanResult, setScanResult] = useState<string | null>(null);
    const [status, setStatus] = useState<"idle" | "loading" | "success" | "error" | "complete">("idle");
    const [message, setMessage] = useState<string>("");
    const [scannedCount, setScannedCount] = useState(0);
    const [totalOrders, setTotalOrders] = useState(0);
    const [currentShipment, setCurrentShipment] = useState<Shipment | null>(null);
    const [scannedOrderIds, setScannedOrderIds] = useState<Set<string>>(new Set());

    // Fetch pending shipment details on mount
    useEffect(() => {
        const fetchShipment = async () => {
            try {
                const token = getAccessToken();
                let foundShipment: Shipment | null = null;

                // 1. Try pending shipments
                const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/driver/pending-shipments`, {
                    headers: { Authorization: `Bearer ${token}` }
                });

                if (response.ok) {
                    const data = await response.json();
                    if (data && data.length > 0) {
                        foundShipment = data[0];
                    }
                }

                // 2. If no pending, check active (IN_TRANSIT) in daily schedule
                if (!foundShipment) {
                    const scheduleResponse = await fetch(`${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/api/driver/schedule`, {
                        headers: { Authorization: `Bearer ${token}` }
                    });
                    if (scheduleResponse.ok) {
                        const schedule = await scheduleResponse.json();
                        // Find first shipment that is NOT delivered (IN_TRANSIT or PENDING)
                        // The schedule DTO structure: { deliveries: [{ shipmentId, status, orders: [...] }] }
                        const activeDelivery = schedule.deliveries?.find((d: any) => d.status !== 'DELIVERED');

                        if (activeDelivery) {
                            foundShipment = {
                                id: activeDelivery.shipmentId,
                                shipmentCode: activeDelivery.shipmentId.toString(),
                                totalOrders: activeDelivery.orders ? activeDelivery.orders.length : (activeDelivery.totalOrders || 0),
                                status: activeDelivery.status
                            };
                        }
                    }
                }

                if (foundShipment) {
                    setCurrentShipment(foundShipment);
                    // Ensure totalOrders is at least 1 if we have a shipment but count is 0 (fallback)
                    setTotalOrders(foundShipment.totalOrders || 1);
                }
            } catch (error) {
                console.error("Failed to fetch shipment details:", error);
            }
        };
        fetchShipment();
    }, []);

    // Auto-redirect on completion
    useEffect(() => {
        if (status === "complete") {
            const timer = setTimeout(() => {
                router.push("/");
            }, 2000);
            return () => clearTimeout(timer);
        }
    }, [status, router]);

    const handleScanSuccess = async (decodedText: string) => {
        if (status === "loading" || status === "success" || status === "complete") return;

        // Extract ID
        let orderId = decodedText;
        if (decodedText.includes("/")) {
            const cleanText = decodedText.endsWith("/") ? decodedText.slice(0, -1) : decodedText;
            const parts = cleanText.split("/");
            for (let i = parts.length - 1; i >= 0; i--) {
                if (!isNaN(Number(parts[i])) && parts[i].trim() !== "") {
                    orderId = parts[i];
                    break;
                }
            }
        }

        if (scannedOrderIds.has(orderId)) {
            setStatus("error");
            setMessage("This package has already been scanned.");
            return;
        }

        setScanResult(decodedText);
        setStatus("loading");
        setMessage("Processing scan...");

        try {
            const token = getAccessToken();
            const response = await fetch(`/api/driver/orders/${orderId}/confirm`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
            });

            if (response.ok) {
                const data = await response.json();

                // Update progress - don't increment scannedCount if already confirmed
                if (!data.alreadyConfirmed) {
                    const newScannedIds = new Set(scannedOrderIds);
                    newScannedIds.add(orderId);
                    setScannedOrderIds(newScannedIds);

                    const newCount = scannedCount + 1;
                    setScannedCount(newCount);
                }

                // Check if complete using backend's allOrdersConfirmed flag
                if (data.allOrdersConfirmed) {
                    setStatus("complete");
                    setMessage("All packages scanned! Redirecting...");
                } else {
                    setStatus("success");
                    const remaining = data.remainingOrdersToScan || 0;
                    setMessage(`Package confirmed! ${remaining} package${remaining !== 1 ? 's' : ''} remaining`);

                    // Auto-reset for next scan after 1.5s
                    setTimeout(() => {
                        setStatus("idle");
                        setScanResult(null);
                        setMessage("");
                    }, 1500);
                }
            } else {
                const errorData = await response.json().catch(() => ({}));
                setStatus("error");
                setMessage(errorData.message || `Failed to confirm order (${response.status}).`);
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

    const handleFinish = () => {
        router.push("/");
    };

    return (
        <div className="min-h-screen bg-slate-50 flex flex-col">
            {/* Header */}
            <header className="bg-white shadow-sm p-4 flex items-center sticky top-0 z-10">
                <Link href="/" className="p-2 mr-2 text-slate-600 hover:bg-slate-100 rounded-full">
                    <ArrowLeft size={24} />
                </Link>
                <div className="flex-1">
                    <h1 className="text-xl font-bold text-slate-800">Scan Packages</h1>
                    {totalOrders > 0 && (
                        <p className="text-xs text-slate-500">
                            {scannedCount} of {totalOrders} scanned
                        </p>
                    )}
                </div>
            </header>

            {/* Content */}
            <main className="flex-1 p-4 flex flex-col items-center">

                {/* Progress Bar */}
                {totalOrders > 0 && (
                    <div className="w-full max-w-md mb-6">
                        <div className="h-2 bg-slate-200 rounded-full overflow-hidden">
                            <div
                                className="h-full bg-blue-600 transition-all duration-500"
                                style={{ width: `${Math.min((scannedCount / totalOrders) * 100, 100)}%` }}
                            />
                        </div>
                    </div>
                )}

                {status === "idle" && (
                    <div className="w-full max-w-md space-y-4">
                        <div className="bg-white p-4 rounded-xl shadow-sm border border-slate-200 text-center mb-4">
                            <Package className="w-8 h-8 text-blue-500 mx-auto mb-2" />
                            <p className="text-slate-600">
                                Scan each package QR code to verify shipment contents.
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
                    <div className="flex flex-col items-center justify-center space-y-4 p-8 bg-white rounded-xl shadow-md mt-10">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
                        <p className="text-lg font-medium text-slate-700">Verifying...</p>
                    </div>
                )}

                {status === "success" && (
                    <div className="flex flex-col items-center justify-center space-y-6 p-8 bg-white rounded-xl shadow-md w-full max-w-md mt-10">
                        <div className="rounded-full bg-green-100 p-4">
                            <CheckCircle size={48} className="text-green-600" />
                        </div>
                        <div className="text-center">
                            <h2 className="text-2xl font-bold text-slate-800 mb-2">Scanned!</h2>
                            <p className="text-slate-600">{message}</p>
                        </div>
                        <button
                            onClick={handleReset}
                            className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
                        >
                            Scan Next Package
                        </button>
                    </div>
                )}

                {status === "complete" && (
                    <div className="flex flex-col items-center justify-center space-y-6 p-8 bg-white rounded-xl shadow-md w-full max-w-md mt-10">
                        <div className="rounded-full bg-green-100 p-4">
                            <CheckCircle size={64} className="text-green-600" />
                        </div>
                        <div className="text-center">
                            <h2 className="text-2xl font-bold text-slate-800 mb-2">Shipment Accepted!</h2>
                            <p className="text-slate-600">All packages have been verified.</p>
                        </div>
                        <button
                            onClick={handleFinish}
                            className="w-full py-3 bg-black hover:bg-gray-800 text-white font-medium rounded-lg transition-colors"
                        >
                            Go to Dashboard
                        </button>
                    </div>
                )}

                {status === "error" && (
                    <div className="flex flex-col items-center justify-center space-y-6 p-8 bg-white rounded-xl shadow-md w-full max-w-md mt-10">
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
