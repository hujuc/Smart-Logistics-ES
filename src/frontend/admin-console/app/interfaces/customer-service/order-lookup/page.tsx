"use client"

import { useState, useEffect } from "react"
import { useSearchParams } from "next/navigation"
import { featureFlagManager } from "@/lib/flagsmith"
import OrderLookupVersionA from "./version-a"
import OrderLookupVersionB from "./version-b"
import { Button } from "@/components/ui/button"
import { Settings } from "lucide-react"

/**
 * Order Lookup Page with Feature Flagging
 * 
 * This component uses Flagsmith to show different versions of the order lookup interface
 * for A/B testing purposes. Users are automatically assigned to version A or B.
 */
export default function OrderLookupPage() {
  const searchParams = useSearchParams()
  const orderIdFromUrl = searchParams.get('orderId')
  
  const [version, setVersion] = useState<'A' | 'B'>('A')
  const [showVersionToggle, setShowVersionToggle] = useState(false)

  useEffect(() => {
    // Initialize feature flags
    // In production, you would get the user ID from authentication
    const userId = 'customer_service'; // Mock user ID
    
    featureFlagManager.initialize(userId).then(() => {
      const assignedVersion = featureFlagManager.getFlag('order-lookup-version')
      setVersion(assignedVersion)
    })

    // Show version toggle in development (for testing)
    if (process.env.NODE_ENV === 'development') {
      setShowVersionToggle(true)
    }
  }, [])

  const handleToggleVersion = () => {
    const newVersion = version === 'A' ? 'B' : 'A'
    featureFlagManager.setFlag('order-lookup-version', newVersion)
    setVersion(newVersion)
  }

  return (
    <div className="relative">
      {/* Development-only version toggle */}
      {showVersionToggle && (
        <div className="fixed bottom-4 right-4 z-50">
          <div className="bg-white border-2 border-gray-300 rounded-lg shadow-lg p-3">
            <div className="flex items-center gap-2 mb-2">
              <Settings className="w-4 h-4 text-gray-600" />
              <span className="text-xs font-semibold text-gray-700">A/B Test Control</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-600">Version:</span>
              <Button
                size="sm"
                variant={version === 'A' ? 'default' : 'outline'}
                onClick={handleToggleVersion}
                className="h-7 text-xs"
              >
                {version === 'A' ? '✓ A (Card)' : 'A (Card)'}
              </Button>
              <Button
                size="sm"
                variant={version === 'B' ? 'default' : 'outline'}
                onClick={handleToggleVersion}
                className="h-7 text-xs"
              >
                {version === 'B' ? '✓ B (Table)' : 'B (Table)'}
              </Button>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              Testing version: <span className="font-mono font-semibold">{version}</span>
            </p>
          </div>
        </div>
      )}

      {/* Render the appropriate version */}
      {version === 'A' ? (
        <OrderLookupVersionA initialOrderId={orderIdFromUrl ? Number(orderIdFromUrl) : undefined} />
      ) : (
        <OrderLookupVersionB initialOrderId={orderIdFromUrl ? Number(orderIdFromUrl) : undefined} />
      )}
    </div>
  )
}
