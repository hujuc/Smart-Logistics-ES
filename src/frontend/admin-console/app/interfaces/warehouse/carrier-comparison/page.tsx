"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Package, Truck, CheckCircle, Filter, Star, Loader2 } from "lucide-react"
import { Carrier, ShipmentCard, Order } from "@/types"
import { ShipmentCards } from "@/components/logistics/shipment-cards"
import { CarrierCompare } from "@/components/logistics/carrier-compare"
import { warehouseService } from "@/lib/services/warehouse.service"
import { calculateShippingRates } from "@/lib/carrier-api"
import type { RateQuote } from "@/types"

export default function CarrierComparisonPage() {
  const [activeTab, setActiveTab] = useState<"new" | "current" | "all" | "details">("new")
  const [flowStep, setFlowStep] = useState<1 | 2 | 3>(1) // 1: Add Orders, 2: Choose Carrier, 3: Confirm
  const [searchTerm, setSearchTerm] = useState("")
  const [currentTabSearchTerm, setCurrentTabSearchTerm] = useState("")

  // Real data states
  const [orders, setOrders] = useState<Order[]>([])
  const [shipments, setShipments] = useState<ShipmentCard[]>([])
  const [carriers, setCarriers] = useState<Carrier[]>([])
  const [rateQuotes, setRateQuotes] = useState<RateQuote[]>([])

  // Loading states
  const [loadingOrders, setLoadingOrders] = useState(true)
  const [loadingRates, setLoadingRates] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [selectedShipments, setSelectedShipments] = useState<string[]>([])
  const [selectedCarrier, setSelectedCarrier] = useState<Carrier | null>(null)
  const [currentTabSelectedCarrier, setCurrentTabSelectedCarrier] = useState<Carrier | null>(null)

  // Fetch orders on mount - only CREATED or READY_FOR_PICKUP
  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setLoadingOrders(true)
        // Fetch only orders that need carrier assignment
        const fetchedOrders = await warehouseService.getOrdersByStatus(["CREATED", "READY_FOR_PICKUP"])
        setOrders(fetchedOrders)

        // Convert orders to shipment cards for display
        const shipmentCards: ShipmentCard[] = fetchedOrders.map(order => ({
          id: order.id.toString(),
          weight: "N/A", // TODO: Add weight field to Order type
          destination: order.shippingAddress,
          priority: order.priority || "Medium",
          date: order.createdAt ? new Date(order.createdAt).toLocaleDateString() : "N/A"
        }))

        setShipments(shipmentCards)
        setError(null)
      } catch (err) {
        console.error("Failed to fetch orders:", err)
        setError("Failed to load orders. Please try again.")
      } finally {
        setLoadingOrders(false)
      }
    }

    fetchOrders()
  }, [])

  const handleAddShipment = (id: string) => {
    setSelectedShipments((prev) => 
      prev.includes(id) ? prev.filter((s) => s !== id) : [...prev, id]
    )
  }

  const handleSelectCarrier = (carrier: Carrier) => {
    setSelectedCarrier(carrier)
  }

  const handleNextStep = async () => {
    // If moving from step 1 to step 2, calculate shipping rates
    if (flowStep === 1) {
      await calculateRatesForSelectedShipments()
    }

    if (flowStep < 3) setFlowStep((flowStep + 1) as 1 | 2 | 3)
  }

  // Calculate shipping rates for selected shipments
  const calculateRatesForSelectedShipments = async () => {
    if (selectedShipments.length === 0) return

    try {
      setLoadingRates(true)
      setError(null)

      // Get the first selected order to calculate rates
      // In a real scenario, you might want to batch this or calculate for each
      const selectedOrder = orders.find(o => o.id.toString() === selectedShipments[0])

      if (!selectedOrder) {
        setError("Selected order not found")
        return
      }

      // Build rate request from order data
      // TODO: Warehouse address should come from warehouse configuration
      // Using US addresses for demo as Shippo test account only supports US carriers
      const rateRequest = {
        fromAddress: {
          name: "Warehouse",
          addressLine1: "215 Clayton St",
          city: "San Francisco",
          state: "CA",
          postalCode: "94117",
          country: "US",
        },
        toAddress: {
          name: "Customer",
          addressLine1: "1092 Indian Summer Ct",
          city: "San Jose",
          state: "CA",
          postalCode: "95122",
          country: "US",
        },
        parcels: [{
          weight: selectedOrder.weight || 2.5, // Default to 2.5kg if not set
          weightUnit: selectedOrder.weightUnit || "kg",
          length: selectedOrder.length || 30, // Default dimensions in cm
          width: selectedOrder.width || 20,
          height: selectedOrder.height || 15,
          distanceUnit: selectedOrder.distanceUnit || "cm",
        }]
      }

      const rateResponse = await calculateShippingRates(rateRequest)
      setRateQuotes(rateResponse.rates)

      // Check if we got any rates
      if (!rateResponse.rates || rateResponse.rates.length === 0) {
        console.log("No rates returned. Messages:", rateResponse.messages)
        setError("No shipping rates available. The carrier accounts may not support this route.")
        setCarriers([])
        return
      }

      // Convert rate quotes to carrier format for display
      const carriersFromRates: Carrier[] = rateResponse.rates.map((rate, index) => ({
        id: `RATE_${index}`,
        name: rate.provider, // Use provider instead of carrierCode (which is null)
        service: rate.serviceLevel,
        logo: getCarrierLogo(rate.provider),
        cost: Number(rate.amount),
        transitTime: rate.estimatedDays ? `${rate.estimatedDays} days` : "N/A",
        reliability: 95, // TODO: Calculate from historical data
        features: [],
        totalShipments: 0,
        onTimeDelivery: 95,
      }))

      setCarriers(carriersFromRates)
    } catch (err) {
      console.error("Failed to calculate shipping rates:", err)
      setError("Failed to calculate shipping rates. Please try again.")
    } finally {
      setLoadingRates(false)
    }
  }

  // Helper to get carrier logo based on provider name
  const getCarrierLogo = (provider: string): string => {
    const logos: Record<string, string> = {
      'fedex': '🚚',
      'ups': '📦',
      'dhl': '✈️',
      'usps': '📮',
      'amazon': '📦',
    }
    const key = provider?.toLowerCase() || ''
    return logos[key] || '📦'
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
    <div className="space-y-4 md:space-y-6">
      <div className="flex flex-col sm:flex-row gap-3 sm:justify-between sm:items-center">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-black">Carrier Comparison</h1>
          <p className="text-sm md:text-base text-gray-600 mt-1">
            {loadingOrders ? "Loading orders..." : `Compare carriers for new shipments (${shipments.length})`}
          </p>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}


      {/* Tab Navigation */}
      <div className="bg-white border rounded-lg p-2 shadow-sm overflow-x-auto">
        <div className="flex gap-2 min-w-max sm:min-w-0">
          <Button
            variant={activeTab === "new" ? "default" : "outline"}
            size="sm"
            onClick={() => {
              setActiveTab("new")
              setFlowStep(1)
            }}
            className={activeTab === "new" ? "bg-brand-purple hover:bg-brand-purple-hover whitespace-nowrap" : "whitespace-nowrap"}
          >
            New Shipment ({shipments.length})
          </Button>
          <Button
            variant={activeTab === "current" ? "default" : "outline"}
            size="sm"
            onClick={() => setActiveTab("current")}
            className="whitespace-nowrap"
          >
            Current Shipments ({orders.length})
          </Button>
          <Button
            variant={activeTab === "details" ? "default" : "outline"}
            size="sm"
            onClick={() => setActiveTab("details")}
            className="whitespace-nowrap"
          >
            Carrier Details ({carriers.length})
          </Button>
          <div className="sm:ml-auto">
            <Button variant="outline" size="sm" className="whitespace-nowrap">
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
              {loadingOrders ? (
                <div className="flex justify-center items-center py-12">
                  <Loader2 className="h-8 w-8 animate-spin text-brand-purple" />
                </div>
              ) : shipments.length === 0 ? (
                <div className="bg-white border rounded-lg p-6 shadow-sm text-center">
                  <Package className="h-16 w-16 mx-auto text-gray-300 mb-4" />
                  <p className="text-lg font-medium text-gray-600">No orders available</p>
                  <p className="text-sm text-gray-500 mt-2">Create orders to compare carriers</p>
                </div>
              ) : (
                <>
                  <ShipmentCards
                    shipments={shipments}
                    selectedShipments={selectedShipments}
                    onAddShipment={handleAddShipment}
                  />
                  <div className="flex justify-between items-center bg-white border rounded-lg p-4 shadow-sm">
                    <p className="text-sm text-gray-600">
                      Selected: <span className="font-bold text-black">{selectedShipments.length}</span> order(s)
                    </p>
                    <Button
                      onClick={handleNextStep}
                      disabled={selectedShipments.length === 0 || loadingRates}
                      className="bg-brand-purple hover:bg-brand-purple-hover"
                    >
                      {loadingRates ? (
                        <>
                          <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                          Calculating Rates...
                        </>
                      ) : (
                        "Next: Choose Carrier"
                      )}
                    </Button>
                  </div>
                </>
              )}
            </>
          )}

          {/* Step 2: Choose Carrier */}
          {flowStep === 2 && (
            <>
              {carriers.length === 0 ? (
                <div className="bg-white border rounded-lg p-6 shadow-sm text-center">
                  <Truck className="h-16 w-16 mx-auto text-gray-300 mb-4" />
                  <p className="text-lg font-medium text-gray-600">No carrier rates available</p>
                  <p className="text-sm text-gray-500 mt-2">Unable to calculate shipping rates for the selected orders</p>
                </div>
              ) : (
                <CarrierCompare
                  carriers={carriers}
                  selectedCarrier={selectedCarrier}
                  searchTerm={searchTerm}
                  setSearchTerm={setSearchTerm}
                  handleSelectCarrier={handleSelectCarrier}
                />
              )}
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
                      {shipments
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

