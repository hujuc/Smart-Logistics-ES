"use client"

import Link from "next/link"

export function Navbar() {


  return (
    <nav className="bg-white border-b">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-14 md:h-16 justify-center">
          <div className="flex">
            <div className="flex flex-shrink-0 items-center">
              <Link href="/" className="text-xl md:text-2xl font-bold text-black">
                SLMS
              </Link>
            </div>
          </div>
        </div>
      </div>
    </nav>
  )
}

