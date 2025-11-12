"use client"

import { Package } from "lucide-react"

export function Navbar() {


  return (
    <nav className="bg-white border-b">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-14 md:h-16 justify-center">
          <div className="flex">
            <div className="flex flex-shrink-0 items-center">
              <div className="flex items-center gap-2">
                <Package className="h-8 w-8 text-black" />
                <span className="text-2xl font-bold text-black">SLMS</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}

