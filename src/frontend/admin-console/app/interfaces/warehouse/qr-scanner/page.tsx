"use client"

import { useState, useRef } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Camera, Scan, Package, AlertCircle, CheckCircle2 } from "lucide-react"
import Link from "next/link"

interface ScannedPackage {
  id: string
  trackingNumber: string
  sender: string
  recipient: string
  status: string
  location: string
  weight: string
  scannedAt: string
}

export default function QRScannerPage() {
  const [isScanning, setIsScanning] = useState(false)
  const [showCamera, setShowCamera] = useState(false)
  const [manualInput, setManualInput] = useState("")
  const [scannedPackages, setScannedPackages] = useState<ScannedPackage[]>([])
  const [lastScanned, setLastScanned] = useState<ScannedPackage | null>(null)
  const [scanStatus, setScanStatus] = useState<"success" | "error" | null>(null)
  const videoRef = useRef<HTMLVideoElement>(null)

  // Mock function to simulate QR code scanning
  const simulateScan = (trackingNumber: string) => {
    // Simulate API call to fetch package details
    const mockPackage: ScannedPackage = {
      id: `PKG${Math.floor(Math.random() * 1000).toString().padStart(3, "0")}`,
      trackingNumber: trackingNumber || `TRK${Math.floor(Math.random() * 1000000000)}`,
      sender: "John Doe",
      recipient: "Jane Smith",
      status: "In Warehouse",
      location: `Shelf ${String.fromCharCode(65 + Math.floor(Math.random() * 5))}-${Math.floor(Math.random() * 20) + 1}`,
      weight: `${(Math.random() * 5 + 0.5).toFixed(1)} kg`,
      scannedAt: new Date().toLocaleTimeString(),
    }

    setLastScanned(mockPackage)
    setScannedPackages([mockPackage, ...scannedPackages])
    setScanStatus("success")

    // Reset status after 3 seconds
    setTimeout(() => setScanStatus(null), 3000)
  }

  const startScanning = () => {
    setShowCamera(true)
    setIsScanning(true)
    // In a real app, you would start the camera here
    // For demo purposes, we'll simulate a scan after 2 seconds
    setTimeout(() => {
      simulateScan("")
      setIsScanning(false)
    }, 2000)
  }

  const handleManualScan = () => {
    if (manualInput.trim()) {
      simulateScan(manualInput)
      setManualInput("")
    }
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === "Enter") {
      handleManualScan()
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">QR Code Scanner</h1>
        <Link href="/interfaces/warehouse">
          <Button variant="outline">Back to Warehouse</Button>
        </Link>
      </div>

      {/* Scanner Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Camera View */}
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h2 className="text-xl font-semibold text-black mb-4">Camera Scanner</h2>
          
          {/* Start Camera Button - Only show when camera is not active */}
          {!showCamera && (
            <div className="space-y-4">
              <Button
                onClick={startScanning}
                className="w-full"
                size="lg"
              >
                <Camera className="mr-2 h-5 w-5 " />
                Start Camera Scan
              </Button>
              <p className="text-xs text-gray-500 text-center">
                Click to activate camera and start scanning
              </p>
            </div>
          )}

          {/* Camera View - Only show when camera is active */}
          {showCamera && (
            <>
              <div className="relative aspect-square bg-gray-900 rounded-lg overflow-hidden flex items-center justify-center">
                {isScanning ? (
                  <div className="absolute inset-0 flex items-center justify-center">
                    <div className="relative">
                      <div className="w-64 h-64 border-4 border-blue-500 rounded-lg animate-pulse"></div>
                      <div className="absolute inset-0 flex items-center justify-center">
                        <Scan className="h-32 w-32 text-blue-500 animate-pulse" />
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="text-center text-white">
                    <Camera className="h-24 w-24 mx-auto mb-4 text-gray-400" />
                    <p className="text-gray-400">Camera active</p>
                  </div>
                )}
                <video
                  ref={videoRef}
                  className="hidden absolute inset-0 w-full h-full object-cover"
                  autoPlay
                  playsInline
                />
              </div>
              <div className="mt-4 space-y-3">
                <Button
                  onClick={startScanning}
                  disabled={isScanning}
                  className="w-full"
                  size="lg"
                >
                  {isScanning ? (
                    <>
                      <Scan className="mr-2 h-5 w-5 animate-pulse" />
                      Scanning...
                    </>
                  ) : (
                    <>
                      <Camera className="mr-2 h-5 w-5" />
                      Scan Again
                    </>
                  )}
                </Button>
                <Button
                  onClick={() => setShowCamera(false)}
                  variant="outline"
                  className="w-full"
                >
                  Stop Camera
                </Button>
                <p className="text-xs text-gray-500 text-center">
                  Position the QR code within the camera frame
                </p>
              </div>
            </>
          )}
        </div>

        {/* Manual Input */}
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <h2 className="text-xl font-semibold text-black mb-4">Manual Entry</h2>
          <div className="space-y-4">
            <div>
              <Label htmlFor="tracking">Tracking Number</Label>
              <Input
                id="tracking"
                type="text"
                placeholder="Enter tracking number manually"
                value={manualInput}
                onChange={(e) => setManualInput(e.target.value)}
                onKeyPress={handleKeyPress}
                className="mt-2"
              />
            </div>
            <Button onClick={handleManualScan} className="w-full" size="lg">
              <Package className="mr-2 h-5 w-5" />
              Scan Package
            </Button>
          </div>

          {/* Last Scanned Package */}
          {lastScanned && (
            <div className="mt-6 pt-6 border-t border-gray-200">
              <div className="flex items-start gap-3">
                {scanStatus === "success" ? (
                  <CheckCircle2 className="h-6 w-6 text-green-600 flex-shrink-0 mt-1" />
                ) : scanStatus === "error" ? (
                  <AlertCircle className="h-6 w-6 text-red-600 flex-shrink-0 mt-1" />
                ) : null}
                <div className="flex-1">
                  <h3 className="font-semibold text-green-700 mb-2">Last Scanned Package</h3>
                  <div className="space-y-1 text-sm">
                    <p>
                      <span className="font-medium">Tracking:</span> {lastScanned.trackingNumber}
                    </p>
                    <p>
                      <span className="font-medium">From:</span> {lastScanned.sender}
                    </p>
                    <p>
                      <span className="font-medium">To:</span> {lastScanned.recipient}
                    </p>
                    <p>
                      <span className="font-medium">Location:</span> {lastScanned.location}
                    </p>
                    <p>
                      <span className="font-medium">Weight:</span> {lastScanned.weight}
                    </p>
                    <p className="text-gray-500">Scanned at: {lastScanned.scannedAt}</p>
                  </div>
                  <Link href={`/interfaces/warehouse/package/${lastScanned.id}`}>
                    <Button variant="link" className="px-0 mt-2">
                      View Full Details →
                    </Button>
                  </Link>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Scanned Packages History */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <h2 className="text-xl font-semibold text-black mb-4">
          Scan History ({scannedPackages.length})
        </h2>
        {scannedPackages.length === 0 ? (
          <div className="text-center py-12 text-gray-500">
            <Package className="h-16 w-16 mx-auto mb-4 text-gray-300" />
            <p>No packages scanned yet</p>
            <p className="text-sm mt-2">Scanned packages will appear here</p>
          </div>
        ) : (
          <div className="space-y-3">
            {scannedPackages.map((pkg, index) => (
              <div
                key={`${pkg.id}-${index}`}
                className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
                    <Package className="h-6 w-6 text-blue-600" />
                  </div>
                  <div>
                    <p className="font-semibold text-black">{pkg.trackingNumber}</p>
                    <p className="text-sm text-gray-600">
                      {pkg.sender} → {pkg.recipient}
                    </p>
                    <p className="text-xs text-gray-500">
                      {pkg.location} • {pkg.weight} • {pkg.scannedAt}
                    </p>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <span className="inline-flex items-center rounded-full px-3 py-1 text-xs font-medium bg-blue-100 text-blue-700">
                    {pkg.status}
                  </span>
                  <Link href={`/interfaces/warehouse/package/${pkg.id}`}>
                    <Button variant="ghost" size="sm">
                      View
                    </Button>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
