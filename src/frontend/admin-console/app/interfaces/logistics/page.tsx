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
import { Search } from "lucide-react"

// Mock data for delivery routes
const mockRoutes = [
  {
    id: "RTE001",
    routeNumber: "RT-001",
    driver: "Mike Johnson",
    vehicle: "VAN-123",
    packages: 15,
    status: "In Progress",
    startLocation: "Warehouse A",
    endLocation: "Distribution Center B",
    estimatedTime: "2.5 hours",
  },
  {
    id: "RTE002",
    routeNumber: "RT-002",
    driver: "Sarah Williams",
    vehicle: "TRK-456",
    packages: 28,
    status: "Completed",
    startLocation: "Warehouse A",
    endLocation: "Distribution Center C",
    estimatedTime: "3.0 hours",
  },
  {
    id: "RTE003",
    routeNumber: "RT-003",
    driver: "Tom Brown",
    vehicle: "VAN-789",
    packages: 12,
    status: "Pending",
    startLocation: "Warehouse B",
    endLocation: "Distribution Center A",
    estimatedTime: "1.8 hours",
  },
]

export default function LogisticsPage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [routes] = useState(mockRoutes)

  const filteredRoutes = routes.filter(
    (route) =>
      route.routeNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      route.driver.toLowerCase().includes(searchTerm.toLowerCase()) ||
      route.vehicle.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">Logistics Management</h1>
        <Button>Create New Route</Button>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="space-y-4">
          <div className="flex gap-4">
            <div className="flex-1">
              <Label htmlFor="search">Search Routes</Label>
              <div className="relative mt-2">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <Input
                  id="search"
                  type="text"
                  placeholder="Enter route number, driver, or vehicle..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            <div className="flex items-end">
              <Button variant="outline">Advanced Search</Button>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
        <div className="p-6">
          <h2 className="text-xl font-semibold text-black mb-4">
            Delivery Routes ({filteredRoutes.length})
          </h2>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Route Number</TableHead>
                <TableHead>Driver</TableHead>
                <TableHead>Vehicle</TableHead>
                <TableHead>Packages</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Start Location</TableHead>
                <TableHead>End Location</TableHead>
                <TableHead>Est. Time</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredRoutes.map((route) => (
                <TableRow key={route.id}>
                  <TableCell className="font-medium">
                    {route.routeNumber}
                  </TableCell>
                  <TableCell>{route.driver}</TableCell>
                  <TableCell>{route.vehicle}</TableCell>
                  <TableCell>{route.packages}</TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                        route.status === "Completed"
                          ? "bg-green-100 text-green-700"
                          : route.status === "In Progress"
                          ? "bg-blue-100 text-blue-700"
                          : "bg-yellow-100 text-yellow-700"
                      }`}
                    >
                      {route.status}
                    </span>
                  </TableCell>
                  <TableCell>{route.startLocation}</TableCell>
                  <TableCell>{route.endLocation}</TableCell>
                  <TableCell>{route.estimatedTime}</TableCell>
                  <TableCell>
                    <Button variant="ghost" size="sm">
                      View Details
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {filteredRoutes.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No routes found matching your search.
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

