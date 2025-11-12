"use client"

import { Suspense } from "react"
import { useSearchParams } from "next/navigation"
import OrderLookupVersionB from "./version-b"

/**
 * Order Lookup Page Content
 * Wrapped component that uses useSearchParams
 */
function OrderLookupContent() {
  const searchParams = useSearchParams()
  const orderIdFromUrl = searchParams.get('orderId')
  
  return <OrderLookupVersionB initialOrderId={orderIdFromUrl ? Number(orderIdFromUrl) : undefined} />
}

/**
 * Order Lookup Page
 * 
 * Displays order lookup interface using Table View (Version B - accepted version)
 */
export default function OrderLookupPage() {
  return (
    <Suspense fallback={<div>Loading...</div>}>
      <OrderLookupContent />
    </Suspense>
  )
}
