"use client"

import { useSearchParams } from "next/navigation"
import OrderLookupVersionB from "./version-b"

/**
 * Order Lookup Page
 * 
 * Displays order lookup interface using Table View (Version B - accepted version)
 */
export default function OrderLookupPage() {
  const searchParams = useSearchParams()
  const orderIdFromUrl = searchParams.get('orderId')
  
  return <OrderLookupVersionB initialOrderId={orderIdFromUrl ? Number(orderIdFromUrl) : undefined} />
}
