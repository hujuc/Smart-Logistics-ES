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

// Mock data for customer inquiries
const mockInquiries = [
  {
    id: "INQ001",
    ticketNumber: "TKT123456",
    customerName: "John Doe",
    email: "john.doe@email.com",
    subject: "Package Delivery Issue",
    status: "Open",
    priority: "High",
    createdAt: "2025-10-05",
  },
  {
    id: "INQ002",
    ticketNumber: "TKT123457",
    customerName: "Jane Smith",
    email: "jane.smith@email.com",
    subject: "Tracking Information",
    status: "In Progress",
    priority: "Medium",
    createdAt: "2025-10-04",
  },
  {
    id: "INQ003",
    ticketNumber: "TKT123458",
    customerName: "Bob Williams",
    email: "bob.williams@email.com",
    subject: "Refund Request",
    status: "Resolved",
    priority: "Low",
    createdAt: "2025-10-03",
  },
]

export default function CustomerServicePage() {
  const [searchTerm, setSearchTerm] = useState("")
  const [inquiries] = useState(mockInquiries)

  const filteredInquiries = inquiries.filter(
    (inquiry) =>
      inquiry.ticketNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      inquiry.customerName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      inquiry.subject.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold text-black">Customer Service</h1>
        <Button>New Inquiry</Button>
      </div>

      <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
        <div className="space-y-4">
          <div className="flex gap-4">
            <div className="flex-1">
              <Label htmlFor="search">Search Inquiries</Label>
              <div className="relative mt-2">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                <Input
                  id="search"
                  type="text"
                  placeholder="Enter ticket number, customer name, or subject..."
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
            Customer Inquiries ({filteredInquiries.length})
          </h2>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Ticket Number</TableHead>
                <TableHead>Customer Name</TableHead>
                <TableHead>Email</TableHead>
                <TableHead>Subject</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Priority</TableHead>
                <TableHead>Created</TableHead>
                <TableHead>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredInquiries.map((inquiry) => (
                <TableRow key={inquiry.id}>
                  <TableCell className="font-medium">
                    {inquiry.ticketNumber}
                  </TableCell>
                  <TableCell>{inquiry.customerName}</TableCell>
                  <TableCell>{inquiry.email}</TableCell>
                  <TableCell>{inquiry.subject}</TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                        inquiry.status === "Open"
                          ? "bg-red-100 text-red-700"
                          : inquiry.status === "In Progress"
                          ? "bg-yellow-100 text-yellow-700"
                          : "bg-green-100 text-green-700"
                      }`}
                    >
                      {inquiry.status}
                    </span>
                  </TableCell>
                  <TableCell>
                    <span
                      className={`inline-flex items-center rounded-full px-2 py-1 text-xs font-medium ${
                        inquiry.priority === "High"
                          ? "bg-red-100 text-red-700"
                          : inquiry.priority === "Medium"
                          ? "bg-yellow-100 text-yellow-700"
                          : "bg-blue-100 text-blue-700"
                      }`}
                    >
                      {inquiry.priority}
                    </span>
                  </TableCell>
                  <TableCell>{inquiry.createdAt}</TableCell>
                  <TableCell>
                    <Button variant="ghost" size="sm">
                      View Details
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          {filteredInquiries.length === 0 && (
            <div className="text-center py-8 text-gray-500">
              No inquiries found matching your search.
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
