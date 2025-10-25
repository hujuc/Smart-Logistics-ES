"use client"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Search, TrendingUp, Package, CheckCircle, Star } from "lucide-react"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Carrier } from "@/types"

interface CarrierCompareProps {
  carriers: Carrier[]
  selectedCarrier: Carrier | null
  searchTerm: string
  setSearchTerm: (value: string) => void
  handleSelectCarrier: (carrier: Carrier) => void
  showSearch?: boolean
}

const renderStars = (rating: number) => {
  return (
    <div className="flex items-center gap-1">
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          className={`h-4 w-4 ${
            star <= Math.floor(rating / 20) ? "fill-yellow-400 text-yellow-400" : "text-gray-300"
          }`}
        />
      ))}
      <span className="text-sm text-gray-600 ml-1">({rating}%)</span>
    </div>
  )
}

export function CarrierCompare({
  carriers,
  selectedCarrier,
  searchTerm,
  setSearchTerm,
  handleSelectCarrier,
  showSearch = true,
}: CarrierCompareProps) {
  const filteredCarriers = carriers.filter(
    (carrier) =>
      carrier.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      carrier.service.toLowerCase().includes(searchTerm.toLowerCase())
  )

  return (
    <div className="bg-white border rounded-lg shadow-sm">
      <div className="p-6">
        {showSearch && (
          <div className="mb-4">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
              <Input
                type="text"
                placeholder="Search carriers..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>
        )}

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Carrier</TableHead>
              <TableHead>
                <div className="flex items-center gap-1">
                  Cost
                  <TrendingUp className="h-3 w-3" />
                </div>
              </TableHead>
              <TableHead>
                <div className="flex items-center gap-1">
                  Transit Time
                  <TrendingUp className="h-3 w-3" />
                </div>
              </TableHead>
              <TableHead>
                <div className="flex items-center gap-1">
                  Reliability
                  <TrendingUp className="h-3 w-3" />
                </div>
              </TableHead>
              <TableHead>Features</TableHead>
              <TableHead>Action</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredCarriers.map((carrier) => (
              <TableRow
                key={carrier.id}
                className={`hover:bg-gray-50 ${
                  selectedCarrier?.id === carrier.id ? "bg-purple-50" : ""
                }`}
              >
                <TableCell>
                  <div className="flex items-center gap-3">
                    <div className="text-2xl">{carrier.logo}</div>
                    <div>
                      <p className="font-semibold text-black">{carrier.name}</p>
                      <p className="text-sm text-gray-600">{carrier.service}</p>
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  <span className="text-lg font-semibold">${carrier.cost}</span>
                </TableCell>
                <TableCell>
                  <span className="text-sm">{carrier.transitTime}</span>
                </TableCell>
                <TableCell>{renderStars(carrier.reliability ?? 0)}</TableCell>
                <TableCell>
                  <div className="flex gap-2">
                    <div
                      className="h-8 w-8 bg-yellow-100 rounded flex items-center justify-center"
                      title="Package Protection"
                    >
                      <Package className="h-4 w-4 text-yellow-600" />
                    </div>
                    <div
                      className="h-8 w-8 bg-green-100 rounded flex items-center justify-center"
                      title="Express Delivery"
                    >
                      <CheckCircle className="h-4 w-4 text-green-600" />
                    </div>
                    <div
                      className="h-8 w-8 bg-blue-100 rounded flex items-center justify-center"
                      title="Insurance"
                    >
                      <CheckCircle className="h-4 w-4 text-blue-600" />
                    </div>
                  </div>
                </TableCell>
                <TableCell>
                  <Button
                    size="sm"
                    onClick={() => handleSelectCarrier(carrier)}
                    className={
                      selectedCarrier?.id === carrier.id
                        ? "bg-white hover:bg-white"
                        : "bg-brand-purple hover:bg-brand-purple-hover"
                    }
                  >
                    {selectedCarrier?.id === carrier.id ? "Selected" : "Select"}
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>

        {filteredCarriers.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No carriers found matching your search.
          </div>
        )}
      </div>
    </div>
  )
}

