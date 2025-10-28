"use client"

import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Search, Navigation } from "lucide-react"

interface TrackPackageSearchProps {
  searchTerm: string
  onSearchChange: (value: string) => void
  onTrack?: () => void
}

export function TrackPackageSearch({
  searchTerm,
  onSearchChange,
  onTrack,
}: TrackPackageSearchProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
      <div className="flex gap-4">
        <div className="flex-1">
          <Label htmlFor="search">Track Package</Label>
          <div className="relative mt-2">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
            <Input
              id="search"
              type="text"
              placeholder="Enter tracking number..."
              value={searchTerm}
              onChange={(e) => onSearchChange(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>
        <div className="flex items-end">
          <Button onClick={onTrack}>
            <Navigation className="mr-2 h-4 w-4" />
            Track
          </Button>
        </div>
      </div>
    </div>
  )
}

