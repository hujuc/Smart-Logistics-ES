"use client";

import { useState, useMemo, useEffect } from 'react';
import { MapPin, Package, Phone, Navigation, ChevronDown, ChevronUp, Clock, AlertCircle, ArrowLeft } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { getDailySchedule } from '@/lib/api';
import { useUser } from '@/shared/hooks/use-user';
import { geocodeAddress, buildAddressString } from '@/lib/geocoding';
import mapboxgl from 'mapbox-gl';

mapboxgl.accessToken = process.env.NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN || '';

type Delivery = {
  id: number;
  stopNumber: number;
  customerName: string;
  address: string;
  packageCount: number;
  priority: 'High' | 'Medium' | 'Low';
  timeWindow: string;
  status: string;
  specialInstructions: string;
  phone: string;
  weight: string;
};

function mapStatus(status: string): string {
  switch (status) {
    case 'IN_TRANSIT':
      return 'In Transit';
    case 'DELIVERED':
      return 'Delivered';
    case 'CREATED':
    case 'READY_FOR_PICKUP':
    case 'COLLECTED':
    case 'ASSIGNED_TO_SHIPMENT':
      return 'Pending';
    default:
      return status;
  }
}

export default function DriverDailySchedule() {
  const router = useRouter();
  const user = useUser();
  const [expandedOrder, setExpandedOrder] = useState<number | null>(null);
  const [sortBy, setSortBy] = useState<'route' | 'time' | 'priority'>('route');
  const [deliveriesData, setDeliveriesData] = useState<Delivery[]>([]);
  const [totalStops, setTotalStops] = useState(0);
  const [totalPackages, setTotalPackages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [estimatedTime, setEstimatedTime] = useState<string>('--');
  const [totalDistance, setTotalDistance] = useState<string>('--');
  const [isCalculatingRoute, setIsCalculatingRoute] = useState(false);

  // Calculate route statistics (distance and time)
  const calculateRouteStats = async (scheduleData: any, deliveries: any[]) => {
    try {
      setIsCalculatingRoute(true);
      console.log('📊 Calculating route statistics for', deliveries.length, 'deliveries');

      // Get warehouse location from first delivery
      const firstDelivery = deliveries[0];
      const warehouseAddress = buildAddressString(
        scheduleData.deliveries[0].warehouseAddress.addressLine1,
        scheduleData.deliveries[0].warehouseAddress.addressLine2,
        scheduleData.deliveries[0].warehouseAddress.city,
        scheduleData.deliveries[0].warehouseAddress.stateOrProvince,
        scheduleData.deliveries[0].warehouseAddress.postalCode,
        scheduleData.deliveries[0].warehouseAddress.country
      );

      const warehouseCoords = await geocodeAddress(warehouseAddress);
      if (!warehouseCoords) {
        console.warn('⚠️ Failed to geocode warehouse');
        setIsCalculatingRoute(false);
        return;
      }

      // Geocode all delivery addresses
      const deliveryCoords: Array<[number, number]> = [];
      for (const delivery of scheduleData.deliveries.filter((d: any) => d.status !== 'DELIVERED')) {
        const address = buildAddressString(
          delivery.address || '',
          null,
          delivery.shippingCity || '',
          delivery.shippingStateOrProvince || '',
          delivery.shippingPostalCode || '',
          delivery.shippingCountry || ''
        );
        const coords = await geocodeAddress(address);
        if (coords) {
          deliveryCoords.push([coords.longitude, coords.latitude]);
        }
      }

      if (deliveryCoords.length === 0) {
        console.warn('⚠️ No delivery coordinates found');
        setIsCalculatingRoute(false);
        return;
      }

      // Build coordinates string for Mapbox Directions API
      const allCoordinates = [
        [warehouseCoords.longitude, warehouseCoords.latitude],
        ...deliveryCoords
      ];

      // Limit to 25 coordinates (Mapbox Directions API limit)
      const coordsToUse = allCoordinates.slice(0, 25);
      const coordinatesString = coordsToUse
        .map(coord => `${coord[0]},${coord[1]}`)
        .join(';');

      console.log('📍 Calling Mapbox Directions API for', coordsToUse.length, 'waypoints');

      // Call Mapbox Directions API
      const response = await fetch(
        `https://api.mapbox.com/directions/v5/mapbox/driving/${coordinatesString}?geometries=geojson&overview=full&access_token=${mapboxgl.accessToken}`
      );

      if (!response.ok) {
        throw new Error(`Directions API failed: ${response.status}`);
      }

      const data = await response.json();
      if (!data.routes || data.routes.length === 0) {
        throw new Error('No route found');
      }

      const route = data.routes[0];
      const distanceKm = (route.distance / 1000).toFixed(0);
      const durationHours = Math.floor(route.duration / 3600);
      const durationMinutes = Math.round((route.duration % 3600) / 60);

      const timeString = durationHours > 0
        ? `${durationHours}h ${durationMinutes}min`
        : `${durationMinutes} min`;

      setTotalDistance(`${distanceKm} km`);
      setEstimatedTime(timeString);

      console.log('✅ Route calculated:', {
        distance: `${distanceKm} km`,
        time: timeString,
        stops: coordsToUse.length - 1
      });

    } catch (err) {
      console.error('❌ Error calculating route:', err);
      setEstimatedTime('--');
      setTotalDistance('--');
    } finally {
      setIsCalculatingRoute(false);
    }
  };

  useEffect(() => {
    async function fetchSchedule() {
      try {
        const scheduleData = await getDailySchedule();

        const activeStatuses = [
          'IN_TRANSIT',
          'CREATED',
          'READY_FOR_PICKUP',
          'COLLECTED',
          'ASSIGNED_TO_SHIPMENT',
        ];
        const pendingDeliveries = scheduleData.deliveries.filter(d =>
          activeStatuses.includes(d.status)
        );

        const deliveries: Delivery[] = pendingDeliveries.map(d => ({
          id: d.orderId,
          stopNumber: d.stopNumber,
          customerName: d.customerName || 'Unknown Customer',
          address: d.address,
          packageCount: 1,
          priority: 'Medium' as const,
          timeWindow: '09:00-17:00',
          status: mapStatus(d.status),
          specialInstructions: '',
          phone: d.phone || '',
          weight: d.weight ? `${d.weight} ${d.weightUnit || 'kg'}` : '0 kg'
        }));

        setDeliveriesData(deliveries);
        setTotalStops(pendingDeliveries.length);
        setTotalPackages(pendingDeliveries.length);

        // Calculate route distance and time if there are deliveries
        if (pendingDeliveries.length > 0) {
          calculateRouteStats(scheduleData, pendingDeliveries);
        }
      } catch (err) {
        console.error('Failed to fetch daily schedule:', err);
        setError(err instanceof Error ? err.message : 'Failed to load schedule');

        // Use mock data as fallback
        const mockData: Delivery[] = [
          {
            id: 1,
            stopNumber: 1,
            customerName: 'João Silva',
            address: '123 Rua Street, Aveiro, CA 94520',
            packageCount: 2,
            priority: 'High',
            timeWindow: '09:00-12:00',
            status: 'Pending',
            specialInstructions: 'Ring doorbell twice. Leave at front door if no answer.',
            phone: '+351 912 345 678',
            weight: '2.5 kg'
          },
          {
            id: 2,
            stopNumber: 2,
            customerName: 'Maria Santos',
            address: '456 Ave Street, Aveiro, CA 94521',
            packageCount: 1,
            priority: 'Medium',
            timeWindow: '10:00-14:00',
            status: 'Pending',
            specialInstructions: 'Call before arrival. Gate code: 1234',
            phone: '+351 918 765 432',
            weight: '1.2 kg'
          },
          {
            id: 3,
            stopNumber: 3,
            customerName: 'Carlos Ferreira',
            address: '789 Main Street, Aveiro, CA 94522',
            packageCount: 3,
            priority: 'Low',
            timeWindow: '13:00-17:00',
            status: 'Pending',
            specialInstructions: 'Business hours only. Reception desk.',
            phone: '+351 915 222 333',
            weight: '5.8 kg'
          }
        ];
        setDeliveriesData(mockData);
        setTotalStops(3);
        setTotalPackages(6);
      } finally {
        setLoading(false);
      }
    }

    fetchSchedule();
  }, []);

  const deliveryStats = {
    totalStops: totalStops,
    totalPackages: totalPackages,
    estimatedTime: estimatedTime,
    distance: totalDistance
  };

  // Sorting logic
  const sortedDeliveries = useMemo(() => {
    const sorted = [...deliveriesData];

    switch(sortBy) {
      case 'route':
        return sorted.sort((a, b) => a.stopNumber - b.stopNumber);

      case 'time':
        return sorted.sort((a, b) => {
          const timeA = a.timeWindow.split('-')[0];
          const timeB = b.timeWindow.split('-')[0];
          return timeA.localeCompare(timeB);
        });

      case 'priority':
        const priorityOrder = { High: 0, Medium: 1, Low: 2 };
        return sorted.sort((a, b) => priorityOrder[a.priority] - priorityOrder[b.priority]);

      default:
        return sorted;
    }
  }, [sortBy, deliveriesData]);

  const toggleExpand = (id: number) => {
    setExpandedOrder(expandedOrder === id ? null : id);
  };

  const getPriorityColor = (priority: string) => {
    switch(priority) {
      case 'High': return 'bg-red-100 text-red-700 border-red-200';
      case 'Medium': return 'bg-yellow-100 text-yellow-700 border-yellow-200';
      case 'Low': return 'bg-gray-100 text-gray-700 border-gray-200';
      default: return 'bg-gray-100 text-gray-700 border-gray-200';
    }
  };

  const getStatusColor = (status: string) => {
    switch(status) {
      case 'Delivered': return 'bg-green-100 text-green-700';
      case 'In Transit': return 'bg-blue-100 text-blue-700';
      case 'Pending': return 'bg-gray-100 text-gray-700';
      case 'Failed': return 'bg-red-100 text-red-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const handleCallCustomer = (phone: string) => {
    window.location.href = `tel:${phone}`;
  };

  const handleNavigate = (deliveryId: number) => {
    // Use optimized navigation by default
    router.push(`/interfaces/navigation-optimized?deliveryId=${deliveryId}`);
  };

  const handleViewMap = () => {
    // Navigate to first delivery with optimized route and auto-simulation
    const firstDelivery = sortedDeliveries[0];
    if (firstDelivery) {
      router.push(`/interfaces/navigation-optimized?deliveryId=${firstDelivery.id}`);
    }
  };

  const handleGoToNextOrder = () => {
    // Find first pending delivery
    const nextDelivery = sortedDeliveries.find(d => d.status === 'Pending');
    if (nextDelivery) {
      router.push(`/interfaces/navigation-optimized?deliveryId=${nextDelivery.id}`);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <Package className="h-12 w-12 text-black mx-auto mb-4 animate-pulse" />
          <p className="text-gray-600">Loading schedule...</p>
        </div>
      </div>
    );
  }

  return (
      <div className="min-h-screen bg-white pb-20">
        {/* Header */}
        <div className="bg-black text-white p-4">
          <div className="flex items-center gap-2 mb-3">
            <Package className="h-5 w-5 text-white" />
            <span className="text-base font-bold text-white">SLMS</span>
          </div>
          <h1 className="text-2xl font-bold">Daily Schedule</h1>
          <p className="text-gray-300 text-sm mt-1">
            {user ? `${user.firstName} ${user.lastName}` : 'Driver'} • Driver
          </p>
          <div className="absolute top-4 right-4">
            <Link href="/" className="flex items-center gap-1 text-white hover:text-gray-300">
              <ArrowLeft className="w-5 h-5" />
              Back
            </Link>
          </div>
        </div>

        {error && (
          <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
            <div className="flex">
              <AlertCircle className="h-5 w-5 text-yellow-400" />
              <div className="ml-3">
                <p className="text-sm text-yellow-700">
                  Using mock data. {error}
                </p>
              </div>
            </div>
          </div>
        )}

        {/* Route Summary - Only show if there are deliveries */}
        {sortedDeliveries.length > 0 ? (
          <>
            <div className="bg-white border-b border-gray-200 p-4">
              <div className="grid grid-cols-2 gap-4 mb-4">
                <div>
                  <p className="text-gray-600 text-sm">Total Stops</p>
                  <p className="text-2xl font-bold text-black">{deliveryStats.totalStops}</p>
                </div>
                <div>
                  <p className="text-gray-600 text-sm">Total Packages</p>
                  <p className="text-2xl font-bold text-black">{deliveryStats.totalPackages}</p>
                </div>
                <div>
                  <p className="text-gray-600 text-sm">Est. Time</p>
                  {isCalculatingRoute ? (
                    <div className="flex items-center gap-2">
                      <div className="w-4 h-4 border-2 border-black border-t-transparent rounded-full animate-spin"></div>
                      <p className="text-sm text-gray-500">Calculating...</p>
                    </div>
                  ) : (
                    <p className="text-lg font-semibold text-black">{deliveryStats.estimatedTime}</p>
                  )}
                </div>
                <div>
                  <p className="text-gray-600 text-sm">Distance</p>
                  {isCalculatingRoute ? (
                    <div className="flex items-center gap-2">
                      <div className="w-4 h-4 border-2 border-black border-t-transparent rounded-full animate-spin"></div>
                      <p className="text-sm text-gray-500">Calculating...</p>
                    </div>
                  ) : (
                    <p className="text-lg font-semibold text-black">{deliveryStats.distance}</p>
                  )}
                </div>
              </div>

              <button
                onClick={handleViewMap}
                className="w-full bg-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors"
              >
                Start Navigation
              </button>
            </div>

            {/* Sort Options */}
            <div className="bg-gray-50 border-b border-gray-200 p-4">
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-gray-700">Sort By:</span>
                <select
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value as 'route' | 'time' | 'priority')}
                    className="flex-1 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-black"
                >
                  <option value="route">Route Order</option>
                  <option value="time">Time Window</option>
                  <option value="priority">Priority</option>
                </select>
              </div>
            </div>

            {/* Delivery List */}
            <div className="p-4 space-y-3">
              {sortedDeliveries.map((delivery) => (
              <div
                  key={delivery.id}
                  className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden"
              >
                {/* Delivery Card Header */}
                <div className="p-4">
                  <div className="flex items-start justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-black text-white rounded-full flex items-center justify-center font-bold">
                        {delivery.stopNumber}
                      </div>
                      <div>
                        <h3 className="font-semibold text-black">{delivery.customerName}</h3>
                        <p className="text-sm text-gray-600 flex items-center gap-1 mt-1">
                          <MapPin className="w-4 h-4" />
                          {delivery.address}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 mb-3">
                <span className={`px-3 py-1 rounded-full text-xs font-medium border ${getPriorityColor(delivery.priority)}`}>
                  {delivery.priority} Priority
                </span>
                    <span className={`px-3 py-1 rounded-full text-xs font-medium ${getStatusColor(delivery.status)}`}>
                  {delivery.status}
                </span>
                  </div>

                  <div className="grid grid-cols-2 gap-3 mb-3 text-sm">
                    <div className="flex items-center gap-2 text-gray-600">
                      <Package className="w-4 h-4" />
                      <span>{delivery.packageCount} package{delivery.packageCount > 1 ? 's' : ''}</span>
                    </div>
                    <div className="flex items-center gap-2 text-gray-600">
                      <Clock className="w-4 h-4" />
                      <span>{delivery.timeWindow}</span>
                    </div>
                  </div>

                  {/* Expandable Section */}
                  {expandedOrder === delivery.id && (
                      <div className="mt-4 pt-4 border-t border-gray-200 space-y-3">
                        {delivery.specialInstructions && (
                            <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-3">
                              <div className="flex items-start gap-2">
                                <AlertCircle className="w-5 h-5 text-yellow-600 flex-shrink-0 mt-0.5" />
                                <div>
                                  <p className="text-xs font-semibold text-yellow-800 mb-1">Special Instructions</p>
                                  <p className="text-sm text-yellow-900">{delivery.specialInstructions}</p>
                                </div>
                              </div>
                            </div>
                        )}

                        <div className="grid grid-cols-2 gap-3 text-sm">
                          <div>
                            <p className="text-gray-600">Weight</p>
                            <p className="font-semibold text-black">{delivery.weight}</p>
                          </div>
                          <div>
                            <p className="text-gray-600">Contact</p>
                            <p className="font-semibold text-black">{delivery.phone}</p>
                          </div>
                        </div>

                        <div className="flex gap-2 mt-4">
                          <button
                            onClick={() => handleCallCustomer(delivery.phone)}
                            className="flex-1 flex items-center justify-center gap-2 bg-white border border-gray-300 text-black py-2 rounded-lg font-medium hover:bg-gray-50 transition-colors"
                          >
                            <Phone className="w-4 h-4" />
                            Call
                          </button>
                          <button
                            onClick={() => handleNavigate(delivery.id)}
                            className="flex-1 flex items-center justify-center gap-2 bg-black text-white py-2 rounded-lg font-medium hover:bg-gray-800 transition-colors"
                          >
                            <Navigation className="w-4 h-4" />
                            Navigate
                          </button>
                        </div>
                      </div>
                  )}

                  {/* Expand/Collapse Button */}
                  <button
                      onClick={() => toggleExpand(delivery.id)}
                      className="w-full mt-3 flex items-center justify-center gap-2 text-sm text-gray-600 hover:text-black transition-colors"
                  >
                    {expandedOrder === delivery.id ? (
                        <>
                          <span>Show Less</span>
                          <ChevronUp className="w-4 h-4" />
                        </>
                    ) : (
                        <>
                          <span>Show Details</span>
                          <ChevronDown className="w-4 h-4" />
                        </>
                    )}
                  </button>
                </div>
              </div>
              ))}
            </div>
          </>
        ) : (
          /* Empty State - No deliveries */
          <div className="flex flex-col items-center justify-center p-12 text-center">
            <div className="bg-gray-100 rounded-full p-6 mb-4">
              <Package className="w-16 h-16 text-gray-400" />
            </div>
            <h3 className="text-xl font-bold text-black mb-2">All Deliveries Completed!</h3>
            <p className="text-gray-600 mb-6">
              You have no pending deliveries at this time.
            </p>
            <Link href="/">
              <button className="bg-black text-white px-6 py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors">
                Back to Home
              </button>
            </Link>
          </div>
        )}

        {/* Bottom Navigation - Only show if there are deliveries */}
        {sortedDeliveries.length > 0 && (
          <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 p-4">
            <button
              onClick={handleGoToNextOrder}
              className="w-full bg-black text-white py-4 rounded-lg font-semibold text-lg hover:bg-gray-800 transition-colors"
            >
              Go to Next Order
            </button>
          </div>
        )}
      </div>
  );
}
