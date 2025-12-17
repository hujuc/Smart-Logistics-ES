"use client";

import { Html5QrcodeScanner } from "html5-qrcode";
import { useEffect, useRef, useState } from "react";

interface QrScannerProps {
    onScanSuccess: (decodedText: string, decodedResult: any) => void;
    onScanFailure?: (error: any) => void;
    fps?: number;
    qrbox?: number;
    aspectRatio?: number;
    disableFlip?: boolean;
    verbose?: boolean;
}

const QrScanner = (props: QrScannerProps) => {
    const scannerRef = useRef<Html5QrcodeScanner | null>(null);
    const [scanError, setScanError] = useState<string | null>(null);

    useEffect(() => {
        // Ensure the element exists before initializing
        const elementId = "reader";
        const element = document.getElementById(elementId);

        if (!element) {
            console.error(`Element with id ${elementId} not found`);
            return;
        }

        // Initialize scanner if not already initialized
        if (!scannerRef.current) {
            const config = {
                fps: props.fps || 10,
                qrbox: props.qrbox || 250,
                aspectRatio: props.aspectRatio || 1.0,
                disableFlip: props.disableFlip || false,
            };

            const verbose = props.verbose === true;

            // Create new instance
            const scanner = new Html5QrcodeScanner(elementId, config, verbose);
            scannerRef.current = scanner;

            // Render
            scanner.render(
                (decodedText, decodedResult) => {
                    props.onScanSuccess(decodedText, decodedResult);
                    // Optional: Clear scanner after success if needed, or keep scanning
                    // scanner.clear(); 
                },
                (errorMessage) => {
                    // Only call failure callback if provided
                    if (props.onScanFailure) {
                        props.onScanFailure(errorMessage);
                    }
                    // Avoid setting state on every frame for scanning errors to prevent re-renders
                    // setScanError(errorMessage); 
                }
            );
        }

        // Cleanup function
        return () => {
            if (scannerRef.current) {
                scannerRef.current.clear().catch(error => {
                    console.error("Failed to clear html5-qrcode scanner. ", error);
                });
                scannerRef.current = null;
            }
        };
    }, []); // Empty dependency array to run once on mount

    return (
        <div className="w-full max-w-md mx-auto">
            <div id="reader" className="w-full bg-slate-100 rounded-lg overflow-hidden"></div>
            {scanError && <p className="text-red-500 text-sm mt-2">{scanError}</p>}
        </div>
    );
};

export default QrScanner;
