"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"

export default function CustomerServicePage() {
  const router = useRouter()

  useEffect(() => {
    // Redirect to tickets page
    router.push("/interfaces/customer-service/tickets")
  }, [router])

  return (
    <div className="flex items-center justify-center min-h-[400px]">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
        <p className="text-gray-600">Redirecting to tickets...</p>
      </div>
    </div>
  )
}
