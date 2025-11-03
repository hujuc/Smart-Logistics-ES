"use client"

import { Button } from "@/components/ui/button"
import { Package, Truck, UserCircle } from "lucide-react"
import { useUserRoles } from "@/shared/hooks/use-user"
import { useRouter } from "next/navigation"

export default function Home() {
  const roles = useUserRoles()
  const router = useRouter()

  // Role checking functions for each interface
  const hasWarehouseAccess = () => {
    return roles.some(role =>
      ['warehouse_staff', 'WAREHOUSE_STAFF'].includes(role)
    )
  }

  const hasLogisticsAccess = () => {
    return roles.some(role =>
      ['logistics_manager', 'LOGISTICS_MANAGER'].includes(role)
    )
  }

  const hasCustomerServiceAccess = () => {
    return roles.some(role =>
      ['customer_service', 'CUSTOMER_SERVICE'].includes(role)
    )
  }

  // Handle navigation with role checking
  const handleNavigation = (path: string, checkAccess: () => boolean, interfaceName: string) => {
    if (checkAccess()) {
      router.push(path)
    } else {
      alert(`Access Denied: You don't have permission to access ${interfaceName}. Redirecting to landing page...`)
      window.location.href = 'http://localhost/'
    }
  }

  return (
    <div className="flex min-h-screen flex-col items-center justify-center -ml-4 md:-ml-24 lg:-ml-32 xl:-ml-40">
      <div className="text-center space-y-8">
        <div className="flex items-center justify-center gap-3 mb-4">
          <Package className="h-16 w-16 text-black" />
          <h1 className="text-6xl font-bold text-black">SLMS</h1>
        </div>
        <p className="text-xl text-gray-600 mb-12">
          Smart Logistics Management System
        </p>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 max-w-4xl">
          <div
            onClick={() => handleNavigation('/interfaces/warehouse', hasWarehouseAccess, 'Warehouse')}
            className="bg-white border rounded-lg p-8 shadow-sm hover:shadow-md transition-shadow cursor-pointer"
          >
            <Package className="h-12 w-12 mb-4 mx-auto text-black" />
            <h2 className="text-xl font-semibold text-black mb-2">
              Warehouse
            </h2>
            <p className="text-gray-600">
              Manage packages and inventory
            </p>
            <Button className="mt-4 w-full">Access</Button>
          </div>

          <div
            onClick={() => handleNavigation('/interfaces/logistics', hasLogisticsAccess, 'Logistics')}
            className="bg-white border rounded-lg p-8 shadow-sm hover:shadow-md transition-shadow cursor-pointer"
          >
            <Truck className="h-12 w-12 mb-4 mx-auto text-black" />
            <h2 className="text-xl font-semibold text-black mb-2">
              Logistics
            </h2>
            <p className="text-gray-600">
              Coordinate deliveries and routes
            </p>
            <Button className="mt-4 w-full">Access</Button>
          </div>

          <div
            onClick={() => handleNavigation('/interfaces/customer-service', hasCustomerServiceAccess, 'Customer Service')}
            className="bg-white border rounded-lg p-8 shadow-sm hover:shadow-md transition-shadow cursor-pointer"
          >
            <UserCircle className="h-12 w-12 mb-4 mx-auto text-black" />
            <h2 className="text-xl font-semibold text-black mb-2">
              Customer Service
            </h2>
            <p className="text-gray-600">
              Support and customer management
            </p>
            <Button className="mt-4 w-full">Access</Button>
          </div>
        </div>
      </div>
    </div>
  )
}
