"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Package, Truck, CheckCircle, Loader2, Settings, UserCheck } from "lucide-react"
import { Carrier, ShipmentCard, Order, User } from "@/types"
import { ShipmentCards } from "@/components/logistics/shipment-cards"
import { CarrierCompare } from "@/components/logistics/carrier-compare"
import { warehouseService } from "@/lib/services/warehouse.service"
import { carrierService, type CarrierResponse } from "@/lib/services/carrier.service"
import { shipmentService } from "@/lib/services/shipment.service"
import { calculateShippingRates } from "@/lib/carrier-api"
import { featureFlagManager } from "@/lib/flagsmith"
import { API_ENDPOINTS, apiFetch } from "@/lib/api"
import { formatOrderShippingAddress, hasStructuredAddress } from "@/lib/address"

// Shipment entity type from backend
interface Shipment {
  id: number
  shipmentCode: string
  createdAt: string
  carrierName: string
  carrierCode?: string
  driverId: string
  totalOrders?: number
  totalWeight?: number
  weightUnit?: string
  shippingCost?: number
  transitTime?: string
  estimatedDeliveryDate?: string
  shippoShipmentId?: string
  shipstationOrderId?: string
}

type AddressComponents = {
  line1?: string
  line2?: string
  city?: string
  state?: string
  postalCode?: string
  country?: string
}

function resolveAddressComponents(order: Order): AddressComponents {
  if (hasStructuredAddress(order)) {
    return {
      line1: order.shippingAddressLine1 || order.shippingAddress,
      line2: order.shippingAddressLine2,
      city: order.shippingCity,
      state: order.shippingStateOrProvince,
      postalCode: order.shippingPostalCode,
      country: order.shippingCountry,
    }
  }

  return parseAddressString(order.shippingAddress)
}

function parseAddressString(value?: string | null): AddressComponents {
  if (!value) {
    return {}
  }

  const tokens = value
    .replace(/\n/g, ",")
    .split(",")
    .map((token) => token.trim())
    .filter(Boolean)

  const result: AddressComponents = {}
  if (tokens.length > 0) {
    result.line1 = tokens[0]
  }
  if (tokens.length > 1) {
    result.city = tokens[1]
  }
  if (tokens.length > 2) {
    const statePostal = tokens[tokens.length - 2]
    const parts = statePostal.split(/\s+/)
    if (parts.length >= 2) {
      result.state = parts[0]
      result.postalCode = parts[parts.length - 1]
    } else {
      result.state = statePostal
    }
  }
  if (tokens.length > 3) {
    result.country = tokens[tokens.length - 1]
  }

  return result
}

export default function CarrierComparisonPage() {
  const [activeTab, setActiveTab] = useState<"assign" | "allShipments">("assign")
  const [flowStep, setFlowStep] = useState<1 | 2 | 3 | 4>(1) // 1: Add Orders, 2: Choose Carrier, 3: Select Driver, 4: Confirm
  const [searchTerm, setSearchTerm] = useState("")
  const [carrierViewMode, setCarrierViewMode] = useState<"table" | "cards">("table") // Toggle between views in Step 2
  const [showViewToggle, setShowViewToggle] = useState(false) // Show toggle control in development

  // Real data states
  const [orders, setOrders] = useState<Order[]>([])
  const [allShipmentOrders, setAllShipmentOrders] = useState<Order[]>([])
  const [shipments, setShipments] = useState<ShipmentCard[]>([])
  const [carriers, setCarriers] = useState<Carrier[]>([])
  const [allCarriers, setAllCarriers] = useState<Carrier[]>([])
  const [carrierRecords, setCarrierRecords] = useState<CarrierResponse[]>([])
  const [drivers, setDrivers] = useState<User[]>([])
  const [allShipments, setAllShipments] = useState<Shipment[]>([]) // Real shipment entities from backend

  // Loading states
  const [loadingOrders, setLoadingOrders] = useState(true)
  const [loadingAllShipments, setLoadingAllShipments] = useState(true)
  const [loadingDrivers, setLoadingDrivers] = useState(false)
  const [loadingRates, setLoadingRates] = useState(false)
  const [loadingConfirm, setLoadingConfirm] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [selectedShipments, setSelectedShipments] = useState<string[]>([])
  const [selectedCarrier, setSelectedCarrier] = useState<Carrier | null>(null)
  const [selectedDriver, setSelectedDriver] = useState<User | null>(null)

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

  useEffect(() => {
    const fetchCarriers = async () => {
      try {
        const data = await carrierService.getAllCarriers()
        const shippoEnabled = data.filter((carrier) => carrier.shippoCarrierId || carrier.shippoAccountId)
        setCarrierRecords(shippoEnabled)
        const mapped = shippoEnabled.map((carrier) => ({
          id: carrier.id.toString(),
          name: carrier.name,
          service: carrier.serviceLevelsSummary || carrier.code,
          logo: getCarrierLogo(carrier.name),
          features: [],
        }))
        setAllCarriers(mapped)
      } catch (err) {
        console.error('Failed to load carriers:', err)
      }
    }

    fetchCarriers()
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
          destination: formatOrderShippingAddress(order),
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

  // Fetch all shipments on mount - real shipment entities from backend
  useEffect(() => {
    const fetchAllShipments = async () => {
      try {
        setLoadingAllShipments(true)

        // Fetch real shipment entities
        const shipmentsData = await shipmentService.getAllShipments()
        setAllShipments(shipmentsData)

        // Also fetch orders for backward compatibility (if needed elsewhere)
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

    // If moving from step 2 to step 3, fetch available drivers
    if (flowStep === 2) {
      await fetchDrivers()
    }

    if (flowStep < 4) setFlowStep((flowStep + 1) as 1 | 2 | 3 | 4)
  }

  // Fetch available drivers from warehouse BFF
  const fetchDrivers = async () => {
    try {
      setLoadingDrivers(true)
      setError(null)

      // Fetch drivers from warehouse BFF endpoint
      // This endpoint returns users with DRIVER role only
      const driverUsers = await apiFetch<User[]>(API_ENDPOINTS.warehouse.drivers)

      setDrivers(driverUsers)

      if (driverUsers.length === 0) {
        setError("No drivers available. Please create driver accounts first.")
      }
    } catch (err) {
      console.error("Failed to fetch drivers:", err)
      setError("Failed to load drivers. Please try again.")
    } finally {
      setLoadingDrivers(false)
    }
  }

  const handleSelectDriver = (driver: User) => {
    setSelectedDriver(driver)
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
      const destination = resolveAddressComponents(selectedOrder)
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
          name: selectedOrder.customerId || selectedOrder.reference || "Customer",
          addressLine1: destination.line1 || "Customer Address",
          addressLine2: destination.line2 || undefined,
          city: destination.city || "San Jose",
          state: destination.state || "CA",
          postalCode: destination.postalCode || "95122",
          country: destination.country || "US",
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

      if (carrierRecords.length === 0) {
        setError("No carriers with Shippo integration are available.")
        return
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
      const carriersFromRates: Carrier[] = rateResponse.rates
        .map((rate) => {
          const backendCarrier = carrierRecords.find((carrier) => {
            const provider = rate.provider?.toLowerCase()
            const serviceCode = rate.carrierCode?.toLowerCase()
            return (
              carrier.name.toLowerCase() === provider ||
              carrier.code.toLowerCase() === provider ||
              carrier.code.toLowerCase() === serviceCode ||
              (!!rate.carrierAccount && carrier.shippoAccountId?.toLowerCase() === rate.carrierAccount.toLowerCase())
            )
          })

          if (!backendCarrier) {
            return null
          }

          return {
            id: backendCarrier.id.toString(),
            name: backendCarrier.name,
            service: rate.serviceLevel || backendCarrier.serviceLevelsSummary || backendCarrier.code,
            logo: getCarrierLogo(backendCarrier.name),
            cost: rate.amount ? Number(rate.amount) : undefined,
            transitTime: rate.estimatedDays ? `${rate.estimatedDays} days` : "N/A",
            features: [],
          }
        })
        .filter((carrier): carrier is Carrier => Boolean(carrier))

      const uniqueCarriers = carriersFromRates.filter((carrier, index, self) =>
        self.findIndex((c) => c.id === carrier.id) === index
      )

      setCarriers(uniqueCarriers)

      if (uniqueCarriers.length === 0) {
        setError("No matching carriers available for the returned rates.")
      }
    } catch (err) {
      console.error("Failed to calculate shipping rates:", err)
      setError("Failed to calculate shipping rates. Please try again.")
    } finally {
      setLoadingRates(false)
    }
  }

  const handlePreviousStep = () => {
    if (flowStep > 1) setFlowStep((flowStep - 1) as 1 | 2 | 3 | 4)
  }

  const handleToggleViewMode = (mode: "table" | "cards") => {
    featureFlagManager.setFlag('carrier-view-mode', mode)
    setCarrierViewMode(mode)
  }

  const handleConfirm = async () => {
    if (!selectedCarrier || !selectedDriver || selectedShipments.length === 0) {
      setError("Please select shipments, a carrier, and a driver")
      return
    }

    try {
      setLoadingConfirm(true)
      setError(null)

      // Step 1: Resolve carrier ID (get from DB or create on-the-fly)
      const carrierRecord = carrierRecords.find((carrier) => carrier.id.toString() === selectedCarrier.id)

      if (!carrierRecord) {
        setError("Selected carrier is no longer available.")
        return
      }

      const shipmentCode = `SHIP-${Date.now()}`
      const orderIds = selectedShipments.map(id => parseInt(id))

      const newShipment = await shipmentService.createShipment({
        shipmentCode,
        carrierId: carrierRecord.id,
        driverId: selectedDriver.id,
        orderIds,
        totalOrders: orderIds.length,
        shippingCost: selectedCarrier.cost,
        transitTime: selectedCarrier.transitTime,
      })

      // Step 3: Update order statuses (if needed - backend should handle this)
      // The backend should automatically update orders when they're added to a shipment

      // Show success message
      alert(`✅ Shipment Created Successfully!\n\n` +
        `Shipment Code: ${newShipment.shipmentCode}\n` +
        `Carrier: ${newShipment.carrierName}\n` +
        `Driver: ${selectedDriver.firstName} ${selectedDriver.lastName}\n` +
        `Orders: ${orderIds.length}\n\n` +
        `Orders are now marked as ASSIGNED_TO_SHIPMENT.`)

      // Refresh orders list - only fetch CREATED orders (assigned orders are now ASSIGNED_TO_SHIPMENT)
      const fetchedOrders = await warehouseService.getOrdersByStatus(["CREATED"])
      setOrders(fetchedOrders)

      const shipmentCards: ShipmentCard[] = fetchedOrders.map(order => ({
        id: order.id.toString(),
        weight: "N/A",
        destination: formatOrderShippingAddress(order),
        priority: order.priority || "Medium",
        date: order.createdAt ? new Date(order.createdAt).toLocaleDateString() : "N/A"
      }))

      setShipments(shipmentCards)

      // Refresh all shipments list (fetch real shipment entities)
      const allShipmentsData = await shipmentService.getAllShipments()
      setAllShipments(allShipmentsData)

      // Also refresh orders with assigned status for backward compatibility
      const assignedOrders = await warehouseService.getOrdersByStatus([
        "ASSIGNED_TO_SHIPMENT",
        "IN_TRANSIT",
        "DELIVERED"
      ])
      setAllShipmentOrders(assignedOrders)

      // Reset flow
      setFlowStep(1)
      setSelectedShipments([])
      setSelectedCarrier(null)
      setSelectedDriver(null)
    } catch (err) {
      console.error("Failed to create shipment:", err)
      setError(`Failed to create shipment: ${err instanceof Error ? err.message : 'Unknown error'}. Please try again.`)
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
            <div className="flex items-center justify-center gap-4 md:gap-8">
              <div className="flex flex-col items-center gap-2">
                <div className={`h-12 w-12 md:h-16 md:w-16 rounded-full ${flowStep === 1 ? 'bg-brand-purple' : 'bg-gray-100'} flex items-center justify-center transition-colors`}>
                  <Package className={`h-6 w-6 md:h-8 md:w-8 text-gray-600 ${flowStep === 1 ? 'text-white' : ''}`} />
                </div>
                <span className={`text-xs md:text-sm font-medium ${flowStep === 1 ? 'text-brand-purple' : 'text-gray-600'}`}>Add Orders</span>
              </div>
              <div className={`flex-1 h-0.5 ${flowStep > 1 ? 'bg-brand-purple' : 'bg-gray-300'} relative transition-colors`}>
                <div className={`absolute right-0 top-1/2 transform -translate-y-1/2 w-2 h-2 md:w-3 md:h-3 ${flowStep > 1 ? 'bg-brand-purple' : 'bg-gray-300'} rotate-45 transition-colors`}></div>
              </div>
              <div className="flex flex-col items-center gap-2">
                <div className={`h-12 w-12 md:h-16 md:w-16 rounded-full ${flowStep === 2 ? 'bg-brand-purple' : 'bg-gray-100'} flex items-center justify-center transition-colors`}>
                  <Truck className={`h-6 w-6 md:h-8 md:w-8 ${flowStep === 2 ? 'text-white' : 'text-gray-600'}`} />
                </div>
                <span className={`text-xs md:text-sm font-medium ${flowStep === 2 ? 'text-brand-purple' : 'text-gray-600'}`}>Choose Carrier</span>
              </div>
              <div className={`flex-1 h-0.5 ${flowStep > 2 ? 'bg-brand-purple' : 'bg-gray-300'} relative transition-colors`}>
                <div className={`absolute right-0 top-1/2 transform -translate-y-1/2 w-2 h-2 md:w-3 md:h-3 ${flowStep > 2 ? 'bg-brand-purple' : 'bg-gray-300'} rotate-45 transition-colors`}></div>
              </div>
              <div className="flex flex-col items-center gap-2">
                <div className={`h-12 w-12 md:h-16 md:w-16 rounded-full ${flowStep === 3 ? 'bg-brand-purple' : 'bg-gray-100'} flex items-center justify-center transition-colors`}>
                  <UserCheck className={`h-6 w-6 md:h-8 md:w-8 ${flowStep === 3 ? 'text-white' : 'text-gray-600'}`} />
                </div>
                <span className={`text-xs md:text-sm font-medium ${flowStep === 3 ? 'text-brand-purple' : 'text-gray-600'}`}>Select Driver</span>
              </div>
              <div className={`flex-1 h-0.5 ${flowStep > 3 ? 'bg-brand-purple' : 'bg-gray-300'} relative transition-colors`}>
                <div className={`absolute right-0 top-1/2 transform -translate-y-1/2 w-2 h-2 md:w-3 md:h-3 ${flowStep > 3 ? 'bg-brand-purple' : 'bg-gray-300'} rotate-45 transition-colors`}></div>
              </div>
              <div className="flex flex-col items-center gap-2">
                <div className={`h-12 w-12 md:h-16 md:w-16 rounded-full ${flowStep === 4 ? 'bg-brand-purple' : 'bg-gray-100'} flex items-center justify-center transition-colors`}>
                  <CheckCircle className={`h-6 w-6 md:h-8 md:w-8 ${flowStep === 4 ? 'text-white' : 'text-gray-600'}`} />
                </div>
                <span className={`text-xs md:text-sm font-medium ${flowStep === 4 ? 'text-brand-purple' : 'text-gray-600'}`}>Confirm</span>
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
                                  <div className="border-purple-200 rounded-lg p-3">
                                    <div className="flex justify-between items-center">
                                      <div>
                                        <p className="text-xs text-purple-700 font-medium mb-1">Shipping Cost</p>
                                        <p className="text-2xl font-bold text-purple-900">${carrier.cost}</p>
                                      </div>
                                      {carrier.cost === Math.min(...carriers.map(c => c.cost || Infinity)) && (
                                        <span className="bg-purple-600 text-white text-xs px-2 py-1 rounded-full font-semibold">
                                          BEST PRICE
                                        </span>
                                      )}
                                    </div>
                                  </div>

                                  {/* Transit Time */}
                                  <div className="border border-purple-200 rounded-lg p-3">
                                    <p className="text-xs text-purple-700 font-medium mb-1">Estimated Transit Time</p>
                                    <p className="text-lg font-bold text-purple-900">{carrier.transitTime || 'N/A'}</p>
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

          {/* Step 3: Select Driver */}
          {flowStep === 3 && (
            <>
              {loadingDrivers ? (
                <div className="flex justify-center items-center py-12">
                  <Loader2 className="h-8 w-8 animate-spin text-brand-purple" />
                </div>
              ) : drivers.length === 0 ? (
                <div className="bg-white border rounded-lg p-6 shadow-sm text-center">
                  <UserCheck className="h-16 w-16 mx-auto text-gray-300 mb-4" />
                  <p className="text-lg font-medium text-gray-600">No drivers available</p>
                  <p className="text-sm text-gray-500 mt-2">Please create driver accounts before creating shipments.</p>
                </div>
              ) : (
                <>
                  <div className="bg-white border rounded-lg p-6 shadow-sm">
                    <div className="space-y-4">
                      <div>
                        <h3 className="text-xl font-bold text-black mb-2">Select a Driver</h3>
                        <p className="text-sm text-gray-600">Choose the best available driver for this shipment</p>
                      </div>

                      {/* Driver Selection Grid */}
                      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        {drivers.map((driver) => (
                          <div
                            key={driver.id}
                            className={`border-2 rounded-lg p-4 cursor-pointer transition-all ${
                              selectedDriver?.id === driver.id
                                ? 'border-purple-500 bg-purple-50 shadow-lg'
                                : 'border-gray-200 hover:shadow-md hover:border-purple-300'
                            }`}
                            onClick={() => handleSelectDriver(driver)}
                          >
                            <div className="flex items-start justify-between mb-3">
                              <div className="flex items-center gap-3">
                                <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center">
                                  <UserCheck className="h-6 w-6 text-purple-600" />
                                </div>
                                <div>
                                  <h5 className="text-base font-bold text-black">
                                    {driver.firstName} {driver.lastName}
                                  </h5>
                                  <p className="text-xs text-gray-600">@{driver.username}</p>
                                </div>
                              </div>
                              {selectedDriver?.id === driver.id && (
                                <div className="bg-purple-600 text-white p-1.5 rounded-full">
                                  <CheckCircle className="h-4 w-4" />
                                </div>
                              )}
                            </div>

                            <div className="space-y-2">
                              <div className="flex justify-between text-sm">
                                <span className="text-gray-600">Email:</span>
                                <span className="font-medium text-black truncate ml-2">{driver.email}</span>
                              </div>
                              <div className="flex justify-between text-sm">
                                <span className="text-gray-600">Status:</span>
                                <span className={`font-medium ${driver.enabled ? 'text-green-600' : 'text-red-600'}`}>
                                  {driver.enabled ? 'Active' : 'Inactive'}
                                </span>
                              </div>
                              <div className="flex justify-between text-sm">
                                <span className="text-gray-600">ID:</span>
                                <span className="font-mono text-xs text-gray-500 truncate ml-2">{driver.id}</span>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
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
                      onClick={handleNextStep}
                      disabled={!selectedDriver}
                      className="bg-brand-purple hover:bg-brand-purple-hover"
                    >
                      Next: Confirm Shipment
                    </Button>
                  </div>
                </>
              )}
            </>
          )}

          {/* Step 4: Confirm */}
          {flowStep === 4 && (
            <>
              <div className="bg-white border rounded-lg p-6 shadow-sm">
                <h2 className="text-2xl font-bold text-black mb-6">Shipment Summary</h2>

                <div className="space-y-6">
                  <div>
                    <h3 className="text-lg font-semibold text-black mb-3">Selected Orders ({selectedShipments.length})</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
                      {shipments
                        .filter((s) => selectedShipments.includes(s.id))
                        .map((shipment) => (
                          <div key={shipment.id} className="border rounded-lg p-3">
                            <p className="font-medium text-sm">Order ID: {shipment.id}</p>
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
                            <p className="text-sm text-gray-600">Shipping Cost</p>
                            <p className="text-xl font-bold text-black">${selectedCarrier.cost}</p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-600">Transit Time</p>
                            <p className="text-lg font-semibold text-black">{selectedCarrier.transitTime}</p>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {selectedDriver && (
                    <div>
                      <h3 className="text-lg font-semibold text-black mb-3">Assigned Driver</h3>
                      <div className="border rounded-lg p-4">
                        <div className="flex items-center gap-3 mb-3">
                          <div className="h-12 w-12 rounded-full bg-purple-100 flex items-center justify-center">
                            <UserCheck className="h-6 w-6 text-purple-600" />
                          </div>
                          <div>
                            <p className="font-bold text-lg text-black">
                              {selectedDriver.firstName} {selectedDriver.lastName}
                            </p>
                            <p className="text-gray-600">@{selectedDriver.username}</p>
                          </div>
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-sm text-gray-600">Email</p>
                            <p className="text-sm font-medium text-black">{selectedDriver.email}</p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-600">Status</p>
                            <p className={`text-sm font-medium ${selectedDriver.enabled ? 'text-green-600' : 'text-red-600'}`}>
                              {selectedDriver.enabled ? 'Active' : 'Inactive'}
                            </p>
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
                  Back to Driver Selection
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
          ) : allShipments.length === 0 ? (
            <div className="bg-white border rounded-lg p-6 shadow-sm text-center">
              <Package className="h-16 w-16 mx-auto text-gray-300 mb-4" />
              <p className="text-lg font-medium text-gray-600">No shipments yet</p>
              <p className="text-sm text-gray-500 mt-2">Create shipments by assigning carriers and drivers to orders</p>
            </div>
          ) : (
            <div className="bg-white border rounded-lg p-6 shadow-sm">
              <h3 className="text-xl font-bold text-black mb-4">All Shipments</h3>
              <p className="text-sm text-gray-600 mb-6">Showing {allShipments.length} active shipment(s)</p>

              {/* Display all shipments */}
              <div className="space-y-4">
                {allShipments.map((shipment) => (
                  <div key={shipment.id} className="border-2 rounded-lg p-5 bg-gradient-to-r from-purple-50 to-white hover:shadow-lg transition-all">
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <div className="flex items-center gap-3 mb-2">
                          <Truck className="h-6 w-6 text-purple-600" />
                          <h4 className="text-lg font-bold text-black">{shipment.shipmentCode}</h4>
                          <span className="text-xs px-2 py-1 rounded-full bg-purple-100 text-purple-700">
                            ID: {shipment.id}
                          </span>
                        </div>
                        <p className="text-sm text-gray-500">
                          Created: {new Date(shipment.createdAt).toLocaleString()}
                        </p>
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      {/* Carrier Info */}
                      <div className="border rounded-lg p-3 bg-white">
                        <p className="text-xs text-gray-500 mb-1">CARRIER</p>
                        <p className="font-semibold text-black">{shipment.carrierName}</p>
                        <p className="text-sm text-gray-600">{shipment.carrierCode}</p>
                      </div>

                      {/* Driver Info */}
                      <div className="border rounded-lg p-3 bg-white">
                        <p className="text-xs text-gray-500 mb-1">DRIVER</p>
                        <div className="flex items-center gap-2">
                          <UserCheck className="h-4 w-4 text-purple-600" />
                          <p className="font-semibold text-black truncate" title={shipment.driverId}>
                            {shipment.driverId}
                          </p>
                        </div>
                      </div>

                      {/* Shipment Details */}
                      <div className="border rounded-lg p-3 bg-white">
                        <p className="text-xs text-gray-500 mb-1">DETAILS</p>
                        <p className="text-sm text-gray-700">
                          <strong>{shipment.totalOrders || 0}</strong> orders
                        </p>
                        {shipment.totalWeight && (
                          <p className="text-sm text-gray-600">
                            {shipment.totalWeight} {shipment.weightUnit}
                          </p>
                        )}
                      </div>
                    </div>

                    {/* Cost and Transit Info */}
                    {(shipment.shippingCost || shipment.transitTime || shipment.estimatedDeliveryDate) && (
                      <div className="mt-4 pt-4 border-t grid grid-cols-1 md:grid-cols-3 gap-3">
                        {shipment.shippingCost && (
                          <div>
                            <p className="text-xs text-gray-500">Shipping Cost</p>
                            <p className="text-lg font-bold text-green-600">${shipment.shippingCost.toFixed(2)}</p>
                          </div>
                        )}
                        {shipment.transitTime && (
                          <div>
                            <p className="text-xs text-gray-500">Transit Time</p>
                            <p className="text-sm font-semibold text-black">{shipment.transitTime}</p>
                          </div>
                        )}
                        {shipment.estimatedDeliveryDate && (
                          <div>
                            <p className="text-xs text-gray-500">Est. Delivery</p>
                            <p className="text-sm font-semibold text-black">
                              {new Date(shipment.estimatedDeliveryDate).toLocaleDateString()}
                            </p>
                          </div>
                        )}
                      </div>
                    )}

                    {/* Integration IDs (if present) */}
                    {(shipment.shippoShipmentId || shipment.shipstationOrderId) && (
                      <div className="mt-3 pt-3 border-t">
                        <p className="text-xs text-gray-500 mb-2">INTEGRATION IDs</p>
                        <div className="flex flex-wrap gap-2">
                          {shipment.shippoShipmentId && (
                            <span className="text-xs px-2 py-1 rounded bg-blue-100 text-blue-700 font-mono">
                              Shippo: {shipment.shippoShipmentId}
                            </span>
                          )}
                          {shipment.shipstationOrderId && (
                            <span className="text-xs px-2 py-1 rounded bg-orange-100 text-orange-700 font-mono">
                              ShipStation: {shipment.shipstationOrderId}
                            </span>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
