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
import Link from "next/link"

// Mock data for packages
const mockPackages = [
  {
    id: "PKG001",
    trackingNumber: "TRK123456789",
    sender: "John Doe",
    recipient: "Jane Smith",
    status: "In Warehouse",
    location: "Shelf A-12",
    weight: "2.5 kg",
  },
  {
    id: "PKG002",
    trackingNumber: "TRK987654321",
    sender: "Alice Johnson",
    recipient: "Bob Williams",
    status: "Ready for Dispatch",
    location: "Shelf B-05",
    weight: "1.8 kg",
  },
  {
    id: "PKG003",
    trackingNumber: "TRK456789123",
    sender: "Charlie Brown",
    recipient: "Diana Prince",
    status: "In Warehouse",
    location: "Shelf C-20",
    weight: "3.2 kg",
  },
]

export default function WarehousePage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [packages] = useState(mockPackages)

  const filteredPackages = packages.filter(
    (pkg) =>
      pkg.trackingNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      pkg.sender.toLowerCase().includes(searchTerm.toLowerCase()) ||
      pkg.recipient.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">Warehouse Management</h1>
        <Link href="/interfaces/warehouse/new-package">
          <Button>Add New Package</Button>
        </Link>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="space-y-4">
          <div className="flex gap-4">
            <div className="flex-1">
              <Label htmlFor="search">Search Packages</Label>
              <div className="relative mt-2">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <Input
                  id="search"
                  type="text"
                  placeholder="Enter tracking number, sender, or recipient..."
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
            Package List ({filteredPackages.length})
          </h2>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Tracking Number</TableHead>
                <TableHead>Sender</TableHead>
                <TableHead>Recipient</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Location</TableHead>
                <TableHead>Weight</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredPackages.map((pkg) => (
                <TableRow key={pkg.id}>
                  <TableCell className="font-medium">
                    {pkg.trackingNumber}
                  </TableCell>
                  <TableCell>{pkg.sender}</TableCell>
                  <TableCell>{pkg.recipient}</TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                        pkg.status === "Ready for Dispatch"
                          ? "bg-green-100 text-green-700"
                          : "bg-blue-100 text-blue-700"
                      }`}
                    >
                      {pkg.status}
                    </span>
                  </TableCell>
                  <TableCell>{pkg.location}</TableCell>
                  <TableCell>{pkg.weight}</TableCell>
                  <TableCell>
                    <Link href={`/interfaces/warehouse/package/${pkg.id}`}>
                      <Button variant="ghost" size="sm">
                        View Details
                      </Button>
                    </Link>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {filteredPackages.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No packages found matching your search.
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

