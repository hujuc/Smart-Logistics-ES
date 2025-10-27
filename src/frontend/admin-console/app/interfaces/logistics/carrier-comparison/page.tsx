"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Package, Truck, CheckCircle, Filter, Star } from "lucide-react"
import { Carrier, ShipmentCard } from "@/types"
import { ShipmentCards } from "@/components/logistics/shipment-cards"
import { CarrierCompare } from "@/components/logistics/carrier-compare"

// Mock data for shipment cards
const mockShipments: ShipmentCard[] = [
  { id: "2401851", weight: "3.2 kg", destination: "Lixa", priority: "Medium", date: "01/10/2025" },
  { id: "2401852", weight: "2.8 kg", destination: "Porto", priority: "High", date: "01/10/2025" },
  { id: "2401853", weight: "1.5 kg", destination: "Lisboa", priority: "Low", date: "01/10/2025" },
  { id: "2401854", weight: "4.1 kg", destination: "Braga", priority: "Medium", date: "01/10/2025" },
  { id: "2401855", weight: "2.3 kg", destination: "Coimbra", priority: "High", date: "01/10/2025" },
  { id: "2401856", weight: "3.7 kg", destination: "Aveiro", priority: "Medium", date: "01/10/2025" },
  { id: "2401857", weight: "1.9 kg", destination: "Leiria", priority: "Low", date: "01/10/2025" },
  { id: "2401858", weight: "2.6 kg", destination: "Setúbal", priority: "Medium", date: "01/10/2025" },
]

// Mock data for carriers
const mockCarriers: Carrier[] = [
  {
    id: "CAR001",
    name: "FedEx",
    service: "FedEx Express",
    logo: "🚚",
    cost: 24.5,
    transitTime: "2-3 days",
    reliability: 98,
    features: ["Package Protection", "Express Delivery", "Insurance"],
    totalShipments: 245,
    onTimeDelivery: 98,
  },
  {
    id: "CAR002",
    name: "UPS",
    service: "UPS Ground",
    logo: "📦",
    cost: 19.99,
    transitTime: "3-5 days",
    reliability: 95,
    features: ["Package Protection", "Tracking"],
    totalShipments: 189,
    onTimeDelivery: 95,
  },
  {
    id: "CAR003",
    name: "DHL",
    service: "DHL Express",
    logo: "✈️",
    cost: 28.75,
    transitTime: "1-2 days",
    reliability: 97,
    features: ["Package Protection", "Express Delivery", "Insurance", "International"],
    totalShipments: 167,
    onTimeDelivery: 97,
  },
  {
    id: "CAR004",
    name: "USPS",
    service: "Priority Mail",
    logo: "📮",
    cost: 15.5,
    transitTime: "2-4 days",
    reliability: 92,
    features: ["Tracking"],
    totalShipments: 98,
    onTimeDelivery: 92,
  },
  {
    id: "CAR005",
    name: "Amazon",
    service: "Amazon Logistics",
    logo: "📦",
    cost: 22.0,
    transitTime: "1-3 days",
    reliability: 94,
    features: ["Package Protection", "Express Delivery"],
    totalShipments: 76,
    onTimeDelivery: 94,
  },
]

export default function CarrierComparisonPage() {
  const [activeTab, setActiveTab] = useState<"new" | "current" | "all" | "details">("new")
  const [flowStep, setFlowStep] = useState<1 | 2 | 3>(1) // 1: Add Orders, 2: Choose Carrier, 3: Confirm
  const [searchTerm, setSearchTerm] = useState("")
  const [currentTabSearchTerm, setCurrentTabSearchTerm] = useState("")
  const [carriers] = useState(mockCarriers)
  const [selectedShipments, setSelectedShipments] = useState<string[]>([])
  const [selectedCarrier, setSelectedCarrier] = useState<Carrier | null>(null)
  const [currentTabSelectedCarrier, setCurrentTabSelectedCarrier] = useState<Carrier | null>(null)

  const handleAddShipment = (id: string) => {
    setSelectedShipments((prev) => 
      prev.includes(id) ? prev.filter((s) => s !== id) : [...prev, id]
    )
  }

  const handleSelectCarrier = (carrier: Carrier) => {
    setSelectedCarrier(carrier)
  }

  const handleNextStep = () => {
    if (flowStep < 3) setFlowStep((flowStep + 1) as 1 | 2 | 3)
  }

  const handlePreviousStep = () => {
    if (flowStep > 1) setFlowStep((flowStep - 1) as 1 | 2 | 3)
  }

  const handleConfirm = () => {
    alert(`Order confirmed!\nShipments: ${selectedShipments.join(', ')}\nCarrier: ${selectedCarrier?.name}`)
    // Reset flow
    setFlowStep(1)
    setSelectedShipments([])
    setSelectedCarrier(null)
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

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-black">Carrier Comparison</h1>
          <p className="text-gray-600 mt-1">Compare carriers for new shipments ({mockShipments.length})</p>
        </div>
      </div>


      {/* Tab Navigation */}
      <div className="bg-white border rounded-lg p-2 shadow-sm">
        <div className="flex gap-2">
          <Button
            variant={activeTab === "new" ? "default" : "outline"}
            size="sm"
            onClick={() => {
              setActiveTab("new")
              setFlowStep(1)
            }}
            className={activeTab === "new" ? "bg-brand-purple hover:bg-brand-purple-hover" : ""}
          >
            New Shipment
          </Button>
          <Button
            variant={activeTab === "current" ? "default" : "outline"}
            size="sm"
            onClick={() => setActiveTab("current")}
          >
            Current Shipments (13)
          </Button>
          <Button
            variant={activeTab === "all" ? "default" : "outline"}
            size="sm"
            onClick={() => setActiveTab("all")}
          >
            All Orders (520)
          </Button>
          <Button
            variant={activeTab === "details" ? "default" : "outline"}
            size="sm"
            onClick={() => setActiveTab("details")}
          >
            Carrier Details
          </Button>
          <div className="ml-auto">
            <Button variant="outline" size="sm">
              <Filter className="h-4 w-4 mr-2" />
              Filters
            </Button>
          </div>
        </div>
      </div>

      {/* New Shipment Flow */}
      {activeTab === "new" && (
        <>
          {/* Process Flow */}
          <div className="bg-white border rounded-lg p-6 shadow-sm">
            <div className="flex items-center justify-center gap-8">
              <div className="flex flex-col items-center gap-2">
                <div className={`h-16 w-16 rounded-full ${flowStep === 1 ? 'bg-brand-purple' : 'bg-gray-100'} flex items-center justify-center transition-colors`}>
                  <Package className={`h-8 w-8 text-gray-600 ${flowStep === 1 ? 'text-white' : ''}`} />
                </div>
                <span className={`text-sm font-medium ${flowStep === 1 ? 'text-brand-purple' : 'text-gray-600'}`}>Add Orders</span>
              </div>
              <div className={`flex-1 h-0.5 ${flowStep > 1 ? 'bg-brand-purple' : 'bg-gray-300'} relative transition-colors`}>
                <div className={`absolute right-0 top-1/2 transform -translate-y-1/2 w-3 h-3 ${flowStep > 1 ? 'bg-brand-purple' : 'bg-gray-300'} rotate-45 transition-colors`}></div>
              </div>
              <div className="flex flex-col items-center gap-2">
                <div className={`h-16 w-16 rounded-full ${flowStep === 2 ? 'bg-brand-purple' : 'bg-gray-100'} flex items-center justify-center transition-colors`}>
                  <Truck className={`h-8 w-8 ${flowStep === 2 ? 'text-white' : 'text-gray-600'}`} />
                </div>
                <span className={`text-sm font-medium ${flowStep === 2 ? 'text-brand-purple' : 'text-gray-600'}`}>Choose Carrier</span>
              </div>
              <div className={`flex-1 h-0.5 ${flowStep > 2 ? 'bg-brand-purple' : 'bg-gray-300'} relative transition-colors`}>
                <div className={`absolute right-0 top-1/2 transform -translate-y-1/2 w-3 h-3 ${flowStep > 2 ? 'bg-brand-purple' : 'bg-gray-300'} rotate-45 transition-colors`}></div>
              </div>
              <div className="flex flex-col items-center gap-2">
                <div className={`h-16 w-16 rounded-full ${flowStep === 3 ? 'bg-brand-purple' : 'bg-gray-100'} flex items-center justify-center transition-colors`}>
                  <CheckCircle className={`h-8 w-8 ${flowStep === 3 ? 'text-white' : 'text-gray-600'}`} />
                </div>
                <span className={`text-sm font-medium ${flowStep === 3 ? 'text-brand-purple' : 'text-gray-600'}`}>Confirm</span>
              </div>
            </div>
          </div>

          {/* Step 1: Add Orders */}
          {flowStep === 1 && (
            <>
              <ShipmentCards
                shipments={mockShipments}
                selectedShipments={selectedShipments}
                onAddShipment={handleAddShipment}
              />
              <div className="flex justify-between items-center bg-white border rounded-lg p-4 shadow-sm">
                <p className="text-sm text-gray-600">
                  Selected: <span className="font-bold text-black">{selectedShipments.length}</span> order(s)
                </p>
                <Button 
                  onClick={handleNextStep}
                  disabled={selectedShipments.length === 0}
                  className="bg-brand-purple hover:bg-brand-purple-hover"
                >
                  Next: Choose Carrier
                </Button>
              </div>
            </>
          )}

          {/* Step 2: Choose Carrier */}
          {flowStep === 2 && (
            <>
              <CarrierCompare
                carriers={carriers}
                selectedCarrier={selectedCarrier}
                searchTerm={searchTerm}
                setSearchTerm={setSearchTerm}
                handleSelectCarrier={handleSelectCarrier}
              />
              <div className="flex justify-between items-center bg-white border rounded-lg p-4 shadow-sm">
                <Button 
                  variant="outline"
                  onClick={handlePreviousStep}
                >
                  Back to Orders
                </Button>
                <Button 
                  onClick={handleNextStep}
                  disabled={!selectedCarrier}
                  className="bg-brand-purple hover:bg-brand-purple-hover"
                >
                  Next: Confirm Order
                </Button>
              </div>
            </>
          )}

          {/* Step 3: Confirm */}
          {flowStep === 3 && (
            <>
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <h2 className="text-2xl font-bold text-black mb-6">Order Summary</h2>
                
                <div className="space-y-6">
                  <div>
                    <h3 className="text-lg font-semibold text-black mb-3">Selected Shipments ({selectedShipments.length})</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                      {mockShipments
                        .filter((s) => selectedShipments.includes(s.id))
                        .map((shipment) => (
                          <div key={shipment.id} className="border rounded-lg p-3">
                            <p className="font-medium text-sm">ID: {shipment.id}</p>
                            <p className="text-sm text-gray-600">Destination: {shipment.destination}</p>
                            <p className="text-sm text-gray-600">Weight: {shipment.weight}</p>
                          </div>
                        ))}
                    </div>
                  </div>

                  {selectedCarrier && (
                    <div>
                      <h3 className="text-lg font-semibold text-black mb-3">Selected Carrier</h3>
                      <div className="border rounded-lg p-4">
                        <div className="flex items-center gap-3 mb-3">
                          <div className="text-3xl">{selectedCarrier.logo}</div>
                          <div>
                            <p className="font-bold text-lg text-black">{selectedCarrier.name}</p>
                            <p className="text-gray-600">{selectedCarrier.service}</p>
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-sm text-gray-600">Cost per shipment</p>
                            <p className="text-xl font-bold text-black">${selectedCarrier.cost}</p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-600">Transit Time</p>
                            <p className="text-lg font-semibold text-black">{selectedCarrier.transitTime}</p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-600">Total Cost</p>
                            <p className="text-xl font-bold text-green-600">${((selectedCarrier.cost ?? 0) * selectedShipments.length).toFixed(2)}</p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-600">Reliability</p>
                            {renderStars(selectedCarrier.reliability ?? 0)}
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                </div>
              </div>
              
              <div className="flex justify-between items-center bg-white border rounded-lg p-4 shadow-sm">
                <Button 
                  variant="outline"
                  onClick={handlePreviousStep}
                >
                  Back to Carrier Selection
                </Button>
                <Button 
                  onClick={handleConfirm}
                  className="bg-brand-purple hover:bg-brand-purple-hover"
                >
                  <CheckCircle className="mr-2 h-4 w-4" />
                  Confirm Order
                </Button>
              </div>
            </>
          )}
        </>
      )}

      {/* Current Shipments Tab - Carrier Comparison Table */}
      {activeTab === "current" && (
        <CarrierCompare
          carriers={carriers}
          selectedCarrier={currentTabSelectedCarrier}
          searchTerm={currentTabSearchTerm}
          setSearchTerm={setCurrentTabSearchTerm}
          handleSelectCarrier={setCurrentTabSelectedCarrier}
        />
      )}

      {/* All Orders Tab */}
      {activeTab === "all" && (
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <div className="text-center py-12">
            <Package className="h-16 w-16 mx-auto text-gray-300 mb-4" />
            <p className="text-lg font-medium text-gray-600">All Orders (520)</p>
            <p className="text-sm text-gray-500 mt-2">View and manage all orders in the system</p>
          </div>
        </div>
      )}

      {/* Carrier Details Tab */}
      {activeTab === "details" && (
        <div className="bg-white border rounded-lg p-6 shadow-sm">
          <div className="space-y-4">
            <h3 className="text-lg font-semibold text-black">Carrier Details</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="space-y-2">
                <p className="text-sm font-medium text-gray-600">Historical performance data</p>
                <p className="text-sm font-medium text-gray-600">Recent delivery statistics</p>
              </div>
              <div className="space-y-2">
                <p className="text-sm font-medium text-gray-600">Coverage map</p>
                <p className="text-sm font-medium text-gray-600">Special services available</p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

