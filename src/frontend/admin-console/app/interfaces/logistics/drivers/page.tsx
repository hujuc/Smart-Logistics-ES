"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Users, CheckCircle2, XCircle, Car } from "lucide-react"
import { DriverStatCard } from "@/components/logistics/driver-stats-cards"
import { DriverSearchBar } from "@/components/logistics/driver-search-bar"
import { DriverTable } from "@/components/logistics/driver-table"

// Mock data for drivers
const mockDrivers = [
  {
    id: "DRV001",
    name: "Mike Johnson",
    email: "mike.johnson@slms.com",
    phone: "+1 (555) 123-4567",
    vehicle: "VAN-123",
    status: "Active",
    currentRoute: "RT-001",
    deliveries: 15,
    rating: 4.8,
  },
  {
    id: "DRV002",
    name: "Sarah Williams",
    email: "sarah.williams@slms.com",
    phone: "+1 (555) 234-5678",
    vehicle: "TRK-456",
    status: "Active",
    currentRoute: "RT-002",
    deliveries: 28,
    rating: 4.9,
  },
  {
    id: "DRV003",
    name: "Tom Brown",
    email: "tom.brown@slms.com",
    phone: "+1 (555) 345-6789",
    vehicle: "VAN-789",
    status: "Off Duty",
    currentRoute: "-",
    deliveries: 0,
    rating: 4.7,
  },
  {
    id: "DRV004",
    name: "Emily Davis",
    email: "emily.davis@slms.com",
    phone: "+1 (555) 456-7890",
    vehicle: "TRK-321",
    status: "Active",
    currentRoute: "RT-004",
    deliveries: 22,
    rating: 4.9,
  },
]

export default function DriversPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [drivers] = useState(mockDrivers)

  const filteredDrivers = drivers.filter(
    (driver) =>
      driver.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      driver.vehicle.toLowerCase().includes(searchTerm.toLowerCase()) ||
      driver.email.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const activeDrivers = drivers.filter((d) => d.status === "Active").length

  // Driver stats configuration for dynamic rendering
  const driverStatsConfig = [
    {
      key: "total",
      title: "Total Drivers",
      value: drivers.length,
      icon: Users,
      iconBgColor: "bg-blue-100",
      iconColor: "text-blue-600",
    },
    {
      key: "active",
      title: "Active",
      value: activeDrivers,
      icon: CheckCircle2,
      iconBgColor: "bg-green-100",
      iconColor: "text-green-600",
    },
    {
      key: "offDuty",
      title: "Off Duty",
      value: drivers.length - activeDrivers,
      icon: XCircle,
      iconBgColor: "bg-gray-100",
      iconColor: "text-gray-600",
    },
    {
      key: "vehicles",
      title: "Vehicles",
      value: drivers.length,
      icon: Car,
      iconBgColor: "bg-yellow-100",
      iconColor: "text-yellow-600",
    },
  ]

  return (
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row gap-3 sm:justify-between sm:items-center">
        <h1 className="text-2xl md:text-3xl font-bold text-black">Driver Management</h1>
        <Button className="w-full sm:w-auto">
          <Users className="mr-2 h-4 w-4" />
          Add New Driver
        </Button>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {driverStatsConfig.map((stat) => (
          <DriverStatCard
            key={stat.key}
            title={stat.title}
            value={stat.value}
            icon={stat.icon}
            iconBgColor={stat.iconBgColor}
            iconColor={stat.iconColor}
          />
        ))}
      </div>

      {/* Search Bar */}
      <DriverSearchBar
        searchTerm={searchTerm}
        onSearchChange={setSearchTerm}
        onFilter={() => alert("Opening filters...")}
      />

      {/* Drivers Table */}
      <DriverTable
        drivers={filteredDrivers}
        onViewDriver={(id) => alert(`Viewing driver ${id}`)}
        onContactDriver={(id) => alert(`Contacting driver ${id}`)}
      />
    </div>
  )
}
