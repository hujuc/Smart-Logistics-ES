"use client"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Search } from "lucide-react"

interface DriverSearchBarProps {
  searchTerm: string
  onSearchChange: (value: string) => void
  onFilter?: () => void
}

export function DriverSearchBar({
  searchTerm,
  onSearchChange,
  onFilter,
}: DriverSearchBarProps) {
  return (
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
              onChange={(e) => onSearchChange(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>
        <div className="flex items-end">
          <Button variant="outline" onClick={onFilter}>
            Filter
          </Button>
        </div>
      </div>
    </div>
  )
}

