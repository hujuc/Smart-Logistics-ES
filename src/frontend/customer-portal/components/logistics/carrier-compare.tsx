"use client"

import { useState, useMemo } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Search, ArrowUpDown, ArrowUp, ArrowDown, Package, CheckCircle, Star } from "lucide-react"
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

type SortField = 'cost' | 'transitTime' | 'reliability' | null
type SortOrder = 'asc' | 'desc'

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
  const [sortField, setSortField] = useState<SortField>(null)
  const [sortOrder, setSortOrder] = useState<SortOrder>('asc')

  const handleSort = (field: SortField) => {
    if (sortField === field) {
      // Toggle sort order if clicking the same field
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc')
    } else {
      // Set new field and default to ascending
      setSortField(field)
      setSortOrder('asc')
    }
  }

  const filteredAndSortedCarriers = useMemo(() => {
    let result = carriers.filter(
      (carrier) =>
        carrier.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        carrier.service.toLowerCase().includes(searchTerm.toLowerCase())
    )

    if (sortField) {
      result = [...result].sort((a, b) => {
        let compareValue = 0

        if (sortField === 'cost') {
          const costA = a.cost || 0
          const costB = b.cost || 0
          compareValue = costA - costB
        } else if (sortField === 'transitTime') {
          // Extract numeric value from transit time (e.g., "2-3 days" -> 2)
          const extractDays = (time?: string) => {
            if (!time) return 999
            const match = time.match(/(\d+)/)
            return match ? parseInt(match[1]) : 999
          }
          compareValue = extractDays(a.transitTime) - extractDays(b.transitTime)
        } else if (sortField === 'reliability') {
          const reliabilityA = a.reliability || 0
          const reliabilityB = b.reliability || 0
          compareValue = reliabilityA - reliabilityB
        }

        return sortOrder === 'asc' ? compareValue : -compareValue
      })
    }

    return result
  }, [carriers, searchTerm, sortField, sortOrder])

  const getSortIcon = (field: SortField) => {
    if (sortField !== field) {
      return <ArrowUpDown className="h-3 w-3 text-gray-400" />
    }
    return sortOrder === 'asc'
      ? <ArrowUp className="h-3 w-3 text-brand-purple" />
      : <ArrowDown className="h-3 w-3 text-brand-purple" />
  }

  return (
    <div className="bg-white border border-gray-200 rounded-lg shadow-sm">
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
                <button
                  onClick={() => handleSort('cost')}
                  className="flex items-center gap-1 hover:text-brand-purple transition-colors cursor-pointer"
                >
                  Cost
                  {getSortIcon('cost')}
                </button>
              </TableHead>
              <TableHead>
                <button
                  onClick={() => handleSort('transitTime')}
                  className="flex items-center gap-1 hover:text-brand-purple transition-colors cursor-pointer"
                >
                  Transit Time
                  {getSortIcon('transitTime')}
                </button>
              </TableHead>
              <TableHead>
                <button
                  onClick={() => handleSort('reliability')}
                  className="flex items-center gap-1 hover:text-brand-purple transition-colors cursor-pointer"
                >
                  Reliability
                  {getSortIcon('reliability')}
                </button>
              </TableHead>
              <TableHead>Features</TableHead>
              <TableHead>Action</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredAndSortedCarriers.map((carrier) => (
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

        {filteredAndSortedCarriers.length === 0 && (
          <div className="text-center py-8 text-gray-500">
            No carriers found matching your search.
          </div>
        )}
      </div>
    </div>
  )
}

