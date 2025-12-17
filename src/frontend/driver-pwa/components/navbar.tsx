"use client"

import { Package } from "lucide-react"

export function Navbar() {
  return (
    <nav className="bg-black text-white p-6">
      <div className="flex items-center gap-2">
        <Package className="h-6 w-6 text-white" />
        <span className="text-lg font-bold text-white">SLMS</span>
      </div>
    </nav>
  )
}
