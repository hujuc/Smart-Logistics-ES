"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { Search, Users, Phone, Mail, Car, CheckCircle2, XCircle } from "lucide-react"
import Link from "next/link"

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

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">Driver Management</h1>
        <Button>
          <Users className="mr-2 h-4 w-4" />
          Add New Driver
        </Button>
      </div>

      {/* Statistics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
              <Users className="h-6 w-6 text-blue-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Total Drivers</p>
              <p className="text-2xl font-bold text-black">{drivers.length}</p>
            </div>
          </div>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
              <CheckCircle2 className="h-6 w-6 text-green-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Active</p>
              <p className="text-2xl font-bold text-black">{activeDrivers}</p>
            </div>
          </div>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 bg-gray-100 rounded-lg flex items-center justify-center">
              <XCircle className="h-6 w-6 text-gray-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Off Duty</p>
              <p className="text-2xl font-bold text-black">
                {drivers.length - activeDrivers}
              </p>
            </div>
          </div>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
              <Car className="h-6 w-6 text-yellow-600" />
            </div>
            <div>
              <p className="text-sm text-gray-600">Vehicles</p>
              <p className="text-2xl font-bold text-black">{drivers.length}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Search Bar */}
      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="flex gap-4">
          <div className="flex-1">
            <Label htmlFor="search">Search Drivers</Label>
            <div className="relative mt-2">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <Input
                id="search"
                type="text"
                placeholder="Enter driver name, vehicle, or email..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
          <div className="flex items-end">
            <Button variant="outline">Filter</Button>
          </div>
        </div>
      </div>

      {/* Drivers Table */}
      <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
        <div className="p-6">
          <h2 className="text-xl font-semibold text-black mb-4">
            Driver List ({filteredDrivers.length})
          </h2>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Driver Name</TableHead>
                <TableHead>Contact</TableHead>
                <TableHead>Vehicle</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Current Route</TableHead>
                <TableHead>Deliveries</TableHead>
                <TableHead>Rating</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredDrivers.map((driver) => (
                <TableRow key={driver.id}>
                  <TableCell className="font-medium">{driver.name}</TableCell>
                  <TableCell>
                    <div className="space-y-1">
                      <div className="flex items-center gap-1 text-sm text-gray-600">
                        <Mail className="h-3 w-3" />
                        {driver.email}
                      </div>
                      <div className="flex items-center gap-1 text-sm text-gray-600">
                        <Phone className="h-3 w-3" />
                        {driver.phone}
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex items-center gap-2">
                      <Car className="h-4 w-4 text-gray-400" />
                      {driver.vehicle}
                    </div>
                  </TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                        driver.status === "Active"
                          ? "bg-green-100 text-green-700"
                          : "bg-gray-100 text-gray-700"
                      }`}
                    >
                      {driver.status}
                    </span>
                  </TableCell>
                  <TableCell>{driver.currentRoute}</TableCell>
                  <TableCell>{driver.deliveries}</TableCell>
                  <TableCell>
                    <div className="flex items-center gap-1">
                      <span className="text-yellow-500">★</span>
                      <span className="font-medium">{driver.rating}</span>
                    </div>
                  </TableCell>
                  <TableCell>
                    <div className="flex gap-2">
                      <Button variant="ghost" size="sm">
                        View
                      </Button>
                      <Button variant="ghost" size="sm">
                        <Phone className="h-4 w-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {filteredDrivers.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No drivers found matching your search.
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
