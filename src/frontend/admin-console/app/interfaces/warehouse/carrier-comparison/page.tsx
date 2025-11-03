"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Package, Truck, CheckCircle, Loader2, Settings } from "lucide-react"
import { Carrier, ShipmentCard, Order } from "@/types"
import { ShipmentCards } from "@/components/logistics/shipment-cards"
import { CarrierCompare } from "@/components/logistics/carrier-compare"
import { warehouseService } from "@/lib/services/warehouse.service"
import { calculateShippingRates } from "@/lib/carrier-api"
import { featureFlagManager } from "@/lib/flagsmith"

export default function CarrierComparisonPage() {
  const [activeTab, setActiveTab] = useState<"assign" | "allShipments">("assign")
  const [flowStep, setFlowStep] = useState<1 | 2 | 3>(1) // 1: Add Orders, 2: Choose Carrier, 3: Confirm
  const [searchTerm, setSearchTerm] = useState("")
  const [carrierViewMode, setCarrierViewMode] = useState<"table" | "cards">("table") // Toggle between views in Step 2
  const [showViewToggle, setShowViewToggle] = useState(false) // Show toggle control in development

  // Real data states
  const [orders, setOrders] = useState<Order[]>([])
  const [allShipmentOrders, setAllShipmentOrders] = useState<Order[]>([])
  const [shipments, setShipments] = useState<ShipmentCard[]>([])
  const [carriers, setCarriers] = useState<Carrier[]>([])
  const [allCarriers, setAllCarriers] = useState<Carrier[]>([])

  // Loading states
  const [loadingOrders, setLoadingOrders] = useState(true)
  const [loadingAllShipments, setLoadingAllShipments] = useState(true)
  const [loadingRates, setLoadingRates] = useState(false)
  const [loadingConfirm, setLoadingConfirm] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [selectedShipments, setSelectedShipments] = useState<string[]>([])
  const [selectedCarrier, setSelectedCarrier] = useState<Carrier | null>(null)

  // Initialize Flagsmith feature flags
  useEffect(() => {
    const initializeFlags = async () => {
      // In production, you would get the user ID from authentication
      const userId = 'warehouse_user' // Mock user ID
      
      await featureFlagManager.initialize(userId)
      const assignedViewMode = featureFlagManager.getFlag('carrier-view-mode')
      setCarrierViewMode(assignedViewMode)

      // Show view toggle in development (for testing)
      if (process.env.NODE_ENV === 'development') {
        setShowViewToggle(true)
      }
    }

    initializeFlags()
  }, [])

  // Fetch orders on mount - only CREATED (orders without carrier assigned)
  useEffect(() => {
    const fetchOrders = async () => {
      try {
        setLoadingOrders(true)
        // Fetch only CREATED orders that need carrier assignment
        const fetchedOrders = await warehouseService.getOrdersByStatus(["CREATED"])
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

  // Fetch all shipments on mount - orders that have been assigned carriers
  useEffect(() => {
    const fetchAllShipments = async () => {
      try {
        setLoadingAllShipments(true)
        // Fetch all orders that have been assigned carriers (ASSIGNED_TO_SHIPMENT, IN_TRANSIT, DELIVERED)
        const fetchedOrders = await warehouseService.getOrdersByStatus([
          "ASSIGNED_TO_SHIPMENT",
          "IN_TRANSIT",
          "DELIVERED"
        ])
        setAllShipmentOrders(fetchedOrders)
      } catch (err) {
        console.error("Failed to fetch all shipments:", err)
      } finally {
        setLoadingAllShipments(false)
      }
    }

    fetchAllShipments()
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
        features: [],
      }))

      setCarriers(carriersFromRates)
      
      // Update allCarriers if not already set
      if (allCarriers.length === 0) {
        setAllCarriers(carriersFromRates)
      }
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

  const handleToggleViewMode = (mode: "table" | "cards") => {
    featureFlagManager.setFlag('carrier-view-mode', mode)
    setCarrierViewMode(mode)
  }

  const handleConfirm = async () => {
    if (!selectedCarrier || selectedShipments.length === 0) {
      setError("Please select shipments and a carrier")
      return
    }

    try {
      setLoadingConfirm(true)
      setError(null)

      // Update each selected order with carrier information
      const updatePromises = selectedShipments.map(async (shipmentId) => {
        const orderId = parseInt(shipmentId)
        
        // Assign carrier to the order
        await warehouseService.assignCarrier(
          orderId,
          selectedCarrier.name,
          selectedCarrier.service,
          selectedCarrier.cost || 0,
          selectedCarrier.transitTime || 'N/A'
        )
      })

      await Promise.all(updatePromises)

      // Show success message
      alert(`✅ Successfully assigned ${selectedCarrier.name} (${selectedCarrier.service}) to ${selectedShipments.length} shipment(s)!\n\nOrders are now marked as ASSIGNED_TO_SHIPMENT and removed from the New Shipment list.`)

      // Refresh orders list - only fetch CREATED orders (assigned orders are now ASSIGNED_TO_SHIPMENT)
      const fetchedOrders = await warehouseService.getOrdersByStatus(["CREATED"])
      setOrders(fetchedOrders)

      const shipmentCards: ShipmentCard[] = fetchedOrders.map(order => ({
        id: order.id.toString(),
        weight: "N/A",
        destination: order.shippingAddress,
        priority: order.priority || "Medium",
        date: order.createdAt ? new Date(order.createdAt).toLocaleDateString() : "N/A"
      }))

      setShipments(shipmentCards)

      // Refresh all shipments list
      const allShipments = await warehouseService.getOrdersByStatus([
        "ASSIGNED_TO_SHIPMENT",
        "IN_TRANSIT",
        "DELIVERED"
      ])
      setAllShipmentOrders(allShipments)

    // Reset flow
    setFlowStep(1)
    setSelectedShipments([])
    setSelectedCarrier(null)
    } catch (err) {
      console.error("Failed to assign carrier:", err)
      setError("Failed to assign carrier to shipments. Please try again.")
    } finally {
      setLoadingConfirm(false)
    }
  }

  return (
    <div className="relative space-y-4 md:space-y-6">
      {/* Development-only view toggle control */}
      {showViewToggle && flowStep === 2 && (
        <div className="fixed bottom-4 right-4 z-50">
          <div className="bg-white border-2 border-purple-300 rounded-lg shadow-lg p-3">
            <div className="flex items-center gap-2 mb-2">
              <Settings className="w-4 h-4 text-purple-600" />
              <span className="text-xs font-semibold text-gray-700">A/B Test Control - Carrier View</span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-xs text-gray-600">View Mode:</span>
              <Button
                size="sm"
                variant={carrierViewMode === 'table' ? 'default' : 'outline'}
                onClick={() => handleToggleViewMode('table')}
                className={`h-7 text-xs ${carrierViewMode === 'table' ? 'bg-brand-purple hover:bg-brand-purple-hover' : ''}`}
              >
                {carrierViewMode === 'table' ? '✓ Table' : 'Table'}
              </Button>
              <Button
                size="sm"
                variant={carrierViewMode === 'cards' ? 'default' : 'outline'}
                onClick={() => handleToggleViewMode('cards')}
                className={`h-7 text-xs ${carrierViewMode === 'cards' ? 'bg-brand-purple hover:bg-brand-purple-hover' : ''}`}
              >
                {carrierViewMode === 'cards' ? '✓ Cards' : 'Cards'}
              </Button>
            </div>
            <p className="text-xs text-gray-500 mt-2">
              Testing mode: <span className="font-mono font-semibold">{carrierViewMode}</span>
            </p>
          </div>
        </div>
      )}

      <div className="flex flex-col sm:flex-row gap-3 sm:justify-between sm:items-center">
        <div>
          <h1 className="text-2xl md:text-3xl font-bold text-black">Carrier Comparison</h1>
          <p className="text-sm md:text-base text-gray-600 mt-1">
            {loadingOrders ? "Loading orders..." : `Assign carriers to orders awaiting shipment (${shipments.length} orders)`}
          </p>
        </div>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          {error}
        </div>
      )}


      {/* Tab Navigation with Statistics */}
      <div className="bg-white border rounded-lg p-2 shadow-sm overflow-x-auto">
        <div className="flex gap-2 min-w-max sm:min-w-0 items-center">
          <Button
            variant={activeTab === "assign" ? "default" : "outline"}
            size="sm"
            onClick={() => {
              setActiveTab("assign")
              setFlowStep(1)
            }}
            className={activeTab === "assign" ? "bg-brand-purple hover:bg-brand-purple-hover whitespace-nowrap" : "whitespace-nowrap"}
          >
            Assign Carrier ({shipments.length})
          </Button>
          <Button
            variant={activeTab === "allShipments" ? "default" : "outline"}
            size="sm"
            onClick={() => setActiveTab("allShipments")}
            className={activeTab === "allShipments" ? "bg-brand-purple hover:bg-brand-purple-hover whitespace-nowrap" : "whitespace-nowrap"}
          >
            All Shipments ({allShipmentOrders.length})
          </Button>
          
          {/* Stats badges */}
          <div className="sm:ml-auto flex gap-2 items-center">
            <div className="hidden md:flex items-center gap-1.5 px-3 py-1.5 bg-green-50 border border-green-200 rounded-md">
              <Truck className="h-4 w-4 text-green-600" />
              <span className="text-xs font-medium text-green-700">{allCarriers.length} Carriers</span>
            </div>
            <div className="hidden lg:flex items-center gap-1.5 px-3 py-1.5 bg-blue-50 border border-blue-200 rounded-md">
              <Package className="h-4 w-4 text-blue-600" />
              <span className="text-xs font-medium text-blue-700">Shippo API</span>
            </div>
          </div>
        </div>
      </div>

      {/* Assign Carrier Flow */}
      {activeTab === "assign" && (
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
                  <p className="text-lg font-medium text-gray-600">No orders awaiting carrier assignment</p>
                  <p className="text-sm text-gray-500 mt-2">All orders have been assigned to carriers. Create new orders or check the In Transit tab.</p>
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
                <>
                  {/* View Toggle */}
                  <div className="bg-white border rounded-lg p-3 shadow-sm">
                    <div className="flex items-center justify-between">
                      <div>
                        <h3 className="text-sm font-semibold text-black">Carrier View</h3>
                        <p className="text-xs text-gray-600">Choose your preferred display mode</p>
                      </div>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          variant={carrierViewMode === "table" ? "default" : "outline"}
                          onClick={() => handleToggleViewMode("table")}
                          className={carrierViewMode === "table" ? "bg-brand-purple hover:bg-brand-purple-hover" : ""}
                        >
                          <Package className="h-4 w-4 mr-2" />
                          Table View
                        </Button>
                        <Button
                          size="sm"
                          variant={carrierViewMode === "cards" ? "default" : "outline"}
                          onClick={() => handleToggleViewMode("cards")}
                          className={carrierViewMode === "cards" ? "bg-brand-purple hover:bg-brand-purple-hover" : ""}
                        >
                          <Truck className="h-4 w-4 mr-2" />
                          Detailed View
                        </Button>
                      </div>
                    </div>
                  </div>

                  {/* Table View */}
                  {carrierViewMode === "table" && (
                <CarrierCompare
                  carriers={carriers}
                  selectedCarrier={selectedCarrier}
                  searchTerm={searchTerm}
                  setSearchTerm={setSearchTerm}
                  handleSelectCarrier={handleSelectCarrier}
                />
                  )}

                  {/* Detailed Cards View */}
                  {carrierViewMode === "cards" && (
                    <div className="bg-white border rounded-lg p-6 shadow-sm">
                      <div className="space-y-6">
                        <div>
                          <h3 className="text-xl font-bold text-black mb-2">Available Carriers</h3>
                          <p className="text-sm text-gray-600">Compare carrier rates and transit times in detail</p>
                        </div>

                        {/* Carrier Statistics */}
                        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                          <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
                            <p className="text-sm text-purple-600 font-medium mb-1">Total Carriers</p>
                            <p className="text-3xl font-bold text-purple-900">{carriers.length}</p>
                          </div>
                          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                            <p className="text-sm text-blue-600 font-medium mb-1">Avg Cost</p>
                            <p className="text-3xl font-bold text-blue-900">
                              ${(carriers.reduce((sum, c) => sum + (c.cost || 0), 0) / carriers.length).toFixed(2)}
                            </p>
                          </div>
                          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                            <p className="text-sm text-green-600 font-medium mb-1">Lowest Cost</p>
                            <p className="text-3xl font-bold text-green-900">
                              ${Math.min(...carriers.map(c => c.cost || Infinity)).toFixed(2)}
                            </p>
                          </div>
                          <div className="bg-orange-50 border border-orange-200 rounded-lg p-4">
                            <p className="text-sm text-orange-600 font-medium mb-1">Fastest</p>
                            <p className="text-3xl font-bold text-orange-900">
                              {(() => {
                                const times = carriers
                                  .map(c => c.transitTime?.match(/(\d+)/)?.[0])
                                  .filter(Boolean)
                                  .map(Number)
                                return times.length > 0 ? `${Math.min(...times)}d` : 'N/A'
                              })()}
                            </p>
                          </div>
                        </div>

                        {/* Detailed Carrier Cards */}
                        <div>
                          <h4 className="text-lg font-semibold text-black mb-4">Select a Carrier</h4>
                          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
                            {carriers.map((carrier) => (
                              <div 
                                key={carrier.id} 
                                className={`border-2 rounded-lg p-5 cursor-pointer transition-all ${
                                  selectedCarrier?.id === carrier.id 
                                    ? 'border-purple-500 bg-purple-50 shadow-lg' 
                                    : 'border-gray-200 hover:shadow-lg hover:border-purple-300'
                                }`}
                                onClick={() => handleSelectCarrier(carrier)}
                              >
                                <div className="flex items-start justify-between mb-4">
                                  <div className="flex items-center gap-3">
                                    <div className="text-4xl">{carrier.logo}</div>
                                    <div>
                                      <h5 className="text-lg font-bold text-black">{carrier.name}</h5>
                                      <p className="text-sm text-gray-600">{carrier.service}</p>
                                    </div>
                                  </div>
                                  {selectedCarrier?.id === carrier.id && (
                                    <div className="bg-purple-600 text-white p-2 rounded-full">
                                      <CheckCircle className="h-5 w-5" />
                                    </div>
                                  )}
                                </div>

                                <div className="space-y-3">
                                  {/* Pricing Information */}
                                  <div className="bg-gradient-to-r from-green-50 to-green-100 border border-green-200 rounded-lg p-3">
                                    <div className="flex justify-between items-center">
                                      <div>
                                        <p className="text-xs text-green-700 font-medium mb-1">Shipping Cost</p>
                                        <p className="text-2xl font-bold text-green-900">${carrier.cost}</p>
                                      </div>
                                      {carrier.cost === Math.min(...carriers.map(c => c.cost || Infinity)) && (
                                        <span className="bg-green-600 text-white text-xs px-2 py-1 rounded-full font-semibold">
                                          BEST PRICE
                                        </span>
                                      )}
                                    </div>
                                  </div>

                                  {/* Transit Time */}
                                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                                    <p className="text-xs text-blue-700 font-medium mb-1">Estimated Transit Time</p>
                                    <p className="text-lg font-bold text-blue-900">{carrier.transitTime || 'N/A'}</p>
                                  </div>

                                  {/* Service Information */}
                                  <div className="border-t pt-3">
                                    <p className="text-xs text-gray-500 font-medium mb-2">SERVICE DETAILS</p>
                                    <div className="space-y-1">
                                      <div className="flex justify-between text-sm">
                                        <span className="text-gray-600">Provider:</span>
                                        <span className="font-semibold text-black">{carrier.name}</span>
                                      </div>
                                      <div className="flex justify-between text-sm">
                                        <span className="text-gray-600">Service Level:</span>
                                        <span className="font-semibold text-black">{carrier.service}</span>
                                      </div>
                                      <div className="flex justify-between text-sm">
                                        <span className="text-gray-600">API Source:</span>
                                        <span className="font-semibold text-black">Shippo</span>
                                      </div>
                                    </div>
                                  </div>
                                </div>
                              </div>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                  )}
                </>
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
                          <div className="col-span-2">
                            <p className="text-sm text-gray-600">Total Cost</p>
                            <p className="text-xl font-bold text-green-600">${((selectedCarrier.cost ?? 0) * selectedShipments.length).toFixed(2)}</p>
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
                  disabled={loadingConfirm}
                >
                  Back to Carrier Selection
                </Button>
                <Button 
                  onClick={handleConfirm}
                  disabled={loadingConfirm}
                  className="bg-brand-purple hover:bg-brand-purple-hover"
                >
                  {loadingConfirm ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Assigning Carrier...
                    </>
                  ) : (
                    <>
                  <CheckCircle className="mr-2 h-4 w-4" />
                  Confirm Order
                    </>
                  )}
                </Button>
              </div>
            </>
          )}
        </>
      )}

      {/* All Shipments Tab */}
      {activeTab === "allShipments" && (
        <>
          {loadingAllShipments ? (
            <div className="flex justify-center items-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-brand-purple" />
            </div>
          ) : allShipmentOrders.length === 0 ? (
            <div className="bg-white border rounded-lg p-6 shadow-sm text-center">
              <Package className="h-16 w-16 mx-auto text-gray-300 mb-4" />
              <p className="text-lg font-medium text-gray-600">No shipments yet</p>
              <p className="text-sm text-gray-500 mt-2">Assign carriers to orders to create shipments</p>
            </div>
          ) : (
            <div className="bg-white border rounded-lg p-6 shadow-sm">
              <h3 className="text-xl font-bold text-black mb-4">All Shipments</h3>
              <p className="text-sm text-gray-600 mb-6">Showing {allShipmentOrders.length} shipment(s) with assigned carriers</p>
              
              {/* Group orders by status */}
              <div className="space-y-6">
                {["ASSIGNED_TO_SHIPMENT", "IN_TRANSIT", "DELIVERED"].map((status) => {
                  const ordersWithStatus = allShipmentOrders.filter(order => order.status === status)
                  if (ordersWithStatus.length === 0) return null
                  
                  return (
                    <div key={status} className="border rounded-lg p-4">
                      <div className="flex items-center justify-between mb-4">
                        <h4 className="text-lg font-semibold text-black">
                          {status.replace(/_/g, ' ')} ({ordersWithStatus.length})
                        </h4>
                      </div>
                      
                      <div className="space-y-3">
                        {ordersWithStatus.map((order) => (
                          <div key={order.id} className="border rounded-lg p-4 bg-gray-50 hover:bg-gray-100 transition-colors">
                            <div className="flex justify-between items-start">
                              <div className="flex-1">
                                <div className="flex items-center gap-3 mb-2">
                                  <p className="font-semibold text-black">Order #{order.reference}</p>
                                  <span className={`text-xs px-2 py-1 rounded-full ${
                                    order.priority === "High" ? "bg-red-100 text-red-700" :
                                    order.priority === "Medium" ? "bg-yellow-100 text-yellow-700" :
                                    "bg-green-100 text-green-700"
                                  }`}>
                                    {order.priority} Priority
                                  </span>
                                </div>
                                <p className="text-sm text-gray-600">
                                  <strong>Destination:</strong> {order.shippingAddress}
                                </p>
                                {order.description && (
                                  <p className="text-sm text-gray-600 mt-1">
                                    <strong>Details:</strong> {order.description}
                                  </p>
                                )}
                                {order.trackingNumber && (
                                  <p className="text-sm text-gray-600 mt-1">
                                    <strong>Tracking:</strong> {order.trackingNumber}
                                  </p>
                                )}
                                <p className="text-xs text-gray-500 mt-2">
                                  Created: {order.createdAt ? new Date(order.createdAt).toLocaleString() : 'N/A'}
                                </p>
                              </div>
          </div>
        </div>
                        ))}
                      </div>
              </div>
                  )
                })}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}

