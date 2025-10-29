import { Button } from "@/components/ui/button"
import Link from "next/link"
import { Package, Truck, UserCircle } from "lucide-react"

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center pr-8">
      <div className="text-center space-y-8">
        <div className="flex items-center justify-center gap-3 mb-4">
          <Package className="h-16 w-16 text-black" />
          <h1 className="text-6xl font-bold text-black">SLMS</h1>
        </div>
        <p className="text-xl text-gray-600 mb-12">
          Smart Logistics Management System
        </p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl">
          <Link href="/interfaces/warehouse">
            <div className="bg-white border rounded-lg p-8 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
              <Package className="h-12 w-12 mb-4 mx-auto text-black" />
              <h2 className="text-xl font-semibold text-black mb-2">
                Warehouse
              </h2>
              <p className="text-gray-600">
                Manage packages and inventory
              </p>
              <Button className="mt-4 w-full">Access</Button>
            </div>
          </Link>

          <Link href="/interfaces/logistics">
            <div className="bg-white border rounded-lg p-8 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
              <Truck className="h-12 w-12 mb-4 mx-auto text-black" />
              <h2 className="text-xl font-semibold text-black mb-2">
                Logistics
              </h2>
              <p className="text-gray-600">
                Coordinate deliveries and routes
              </p>
              <Button className="mt-4 w-full">Access</Button>
            </div>
          </Link>

          <Link href="/interfaces/customer-service">
            <div className="bg-white border rounded-lg p-8 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
              <UserCircle className="h-12 w-12 mb-4 mx-auto text-black" />
              <h2 className="text-xl font-semibold text-black mb-2">
                Customer Service
              </h2>
              <p className="text-gray-600">
                Support and customer management
              </p>
              <Button className="mt-4 w-full">Access</Button>
            </div>
          </Link>
        </div>
      </div>
    </div>
  )
}
