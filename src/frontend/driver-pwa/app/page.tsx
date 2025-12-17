"use client";

import { useEffect, useState, useRef } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { getDashboard, DashboardResponse, getPendingShipments, acceptShipment, PendingShipment, getDailySchedule } from '@/lib/api';
import { Truck, Package, MapPin, Clock, Bell, CheckCircle, XCircle, Settings } from 'lucide-react';
import { useUser } from '@/shared/hooks/use-user';
import Alert from '@shared/components/Alert';
import { useAlert } from '@shared/hooks/useAlert';
import { geocodeAddress, buildAddressString } from '@/lib/geocoding';
import mapboxgl from 'mapbox-gl';

mapboxgl.accessToken = process.env.NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN || '';
import { useNotificationsStore, useUnreadNotifications } from '@/shared/hooks/use-notifications';
import { useNotificationAnimation } from '@/shared/hooks/use-notification-animation';
import { useWebSocket } from '@/shared/hooks/use-websocket';
import { getAccessToken } from '@/shared/lib/auth';

export default function DriverHome() {
  const user = useUser();
  const router = useRouter();
  const { alert, showWarning, showSuccess, showError, closeAlert } = useAlert();
  const [dashboardData, setDashboardData] = useState<DashboardResponse | null>(null);
  const [pendingShipments, setPendingShipments] = useState<PendingShipment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Notification state
  const unreadCount = useUnreadNotifications();
  // const { setUnreadCount, incrementUnread } = useNotificationsStore();
  const shouldAnimate = useNotificationAnimation(unreadCount);

  // Initial unread count is handled by global listener

  // WebSocket for real-time updates
  // WebSocket for real-time updates is handled by global listener
  const [acceptingShipmentId, setAcceptingShipmentId] = useState<number | null>(null);
  const [calculatedETA, setCalculatedETA] = useState<string>('--');

  const calculateNextDeliveryETA = async () => {
    try {
      const schedule = await getDailySchedule();
      const pendingDeliveries = schedule.deliveries.filter(d => d.status !== 'DELIVERED');

      if (pendingDeliveries.length === 0) {
        setCalculatedETA('--');
        return;
      }

      const nextDelivery = pendingDeliveries[0];

      // Geocode warehouse (starting point)
      const warehouseAddress = buildAddressString(
        nextDelivery.warehouseAddress.addressLine1,
        nextDelivery.warehouseAddress.addressLine2,
        nextDelivery.warehouseAddress.city,
        nextDelivery.warehouseAddress.stateOrProvince,
        nextDelivery.warehouseAddress.postalCode,
        nextDelivery.warehouseAddress.country
      );

      const warehouseCoords = await geocodeAddress(warehouseAddress);
      if (!warehouseCoords) {
        console.warn('Failed to geocode warehouse');
        setCalculatedETA('--');
        return;
      }

      // Geocode next delivery address
      const deliveryAddress = buildAddressString(
        nextDelivery.address || '',
        null,
        nextDelivery.shippingCity || '',
        nextDelivery.shippingStateOrProvince || '',
        nextDelivery.shippingPostalCode || '',
        nextDelivery.shippingCountry || ''
      );

      const deliveryCoords = await geocodeAddress(deliveryAddress);
      if (!deliveryCoords) {
        console.warn('Failed to geocode delivery address');
        setCalculatedETA('--');
        return;
      }

      // Get route and calculate duration using Mapbox Directions API
      const query = await fetch(
        `https://api.mapbox.com/directions/v5/mapbox/driving/${warehouseCoords.longitude},${warehouseCoords.latitude};${deliveryCoords.longitude},${deliveryCoords.latitude}?access_token=${mapboxgl.accessToken}`,
        { method: 'GET' }
      );

      const json = await query.json();
      if (json.routes && json.routes.length > 0) {
        const durationInSeconds = json.routes[0].duration;
        const totalMinutes = Math.round(durationInSeconds / 60);

        // Format as hours and minutes if over 60 minutes
        if (totalMinutes >= 60) {
          const hours = Math.floor(totalMinutes / 60);
          const minutes = totalMinutes % 60;
          setCalculatedETA(minutes > 0 ? `${hours}h ${minutes}min` : `${hours}h`);
        } else {
          setCalculatedETA(`${totalMinutes} min`);
        }
      } else {
        setCalculatedETA('--');
      }
    } catch (error) {
      console.error('Failed to calculate ETA:', error);
      setCalculatedETA('--');
    }
  };

  // Calculate ETA to next delivery on component mount
  useEffect(() => {
    calculateNextDeliveryETA();
  }, []);

  useEffect(() => {
    async function fetchData() {
      try {
        // Fetch both dashboard and pending shipments
        const [dashData, pendingData] = await Promise.all([
          getDashboard().catch(() => null),
          getPendingShipments().catch(() => [])
        ]);

        if (dashData) {
          setDashboardData(dashData);
        }
        setPendingShipments(pendingData);
      } catch (err) {
        console.error('Failed to fetch data:', err);
        setError(err instanceof Error ? err.message : 'Failed to load data');

        // Use mock data as fallback
        setDashboardData({
          todayStats: {
            totalDeliveries: 25,
            completed: 8,
            remaining: 17,
            nextStopETA: '15 min'
          },
          nextDeliveries: [
            {
              orderId: 1,
              stopNumber: 1,
              customerName: 'João Silva',
              address: '123 Rua Street, Aveiro',
              priority: 'High'
            },
            {
              orderId: 2,
              stopNumber: 2,
              customerName: 'Maria Santos',
              address: '456 Ave Street, Aveiro',
              priority: 'Medium'
            },
            {
              orderId: 3,
              stopNumber: 3,
              customerName: 'Carlos Ferreira',
              address: '789 Main Street, Aveiro',
              priority: 'Low'
            }
          ]
        });
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);

  const handleAcceptShipment = async (shipmentId: number) => {
    setAcceptingShipmentId(shipmentId);
    try {
      await acceptShipment(shipmentId);

      // Remove from pending list
      setPendingShipments(prev => prev.filter(s => s.id !== shipmentId));

      // Refresh dashboard
      const dashData = await getDashboard().catch(() => null);
      if (dashData) setDashboardData(dashData);

      // Recalculate ETA with new deliveries
      await calculateNextDeliveryETA();

      // Show success message
      showSuccess('Shipment Accepted', 'Shipment accepted successfully! You can now view it in your daily schedule.');

      // Reload the page after a short delay to ensure fresh data
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    } catch (err) {
      console.error('Failed to accept shipment:', err);
      showError('Accept Failed', 'Failed to accept shipment: ' + (err instanceof Error ? err.message : 'Unknown error'));
      setAcceptingShipmentId(null);
    }
  };

  const todayStats = dashboardData?.todayStats || {
    totalDeliveries: 0,
    completed: 0,
    remaining: 0,
    nextStopETA: 'N/A'
  };

  const nextDeliveries = dashboardData?.nextDeliveries || [];

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'High': return 'bg-red-100 text-red-700';
      case 'Medium': return 'bg-yellow-100 text-yellow-700';
      case 'Low': return 'bg-gray-100 text-gray-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  };

  const handleStartNavigation = async (e: React.MouseEvent) => {
    e.preventDefault();
    try {
      const schedule = await getDailySchedule();
      const pendingDeliveries = schedule.deliveries.filter(d => d.status !== 'DELIVERED');

      if (pendingDeliveries.length === 0) {
        showWarning('No Packages to Deliver', 'You have no packages to deliver at this time.');
        return;
      }

      router.push('/interfaces/navigation-optimized');
    } catch (error) {
      console.error('Failed to check deliveries:', error);
      showError('Error', 'Failed to load delivery information. Please try again.');
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <Package className="h-12 w-12 text-black mx-auto mb-4 animate-pulse" />
          <p className="text-gray-600">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <Alert
        isOpen={alert.isOpen}
        onClose={closeAlert}
        title={alert.title}
        message={alert.message}
        type={alert.type}
      />
      {/* Header */}
      <div className="bg-white border-b border-gray-200 p-6">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h1 className="text-3xl font-bold text-black">Welcome Back</h1>
            <p className="text-gray-600 text-sm mt-1">
              {user ? `${user.firstName} ${user.lastName}` : 'Driver'} • Driver
            </p>
          </div>
          <div className="flex items-center gap-4">
            <Link href="/notifications" className="relative">
              <Bell className="w-6 h-6 text-black" />
              {unreadCount > 0 && (
                <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-xs font-bold text-white animate-in fade-in zoom-in duration-300">
                  {unreadCount > 9 ? '9+' : unreadCount}
                  {shouldAnimate && (
                    <span className="absolute inline-flex h-full w-full rounded-full bg-red-400 opacity-75 animate-ping"></span>
                  )}
                </span>
              )}
            </Link>
            <Link href="/settings">
              <Settings className="w-6 h-6 text-black" />
            </Link>
          </div>
        </div>
        <p className="text-gray-500 text-sm">
          {new Date().toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
        </p>
      </div>

      {error && (
        <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 mx-6 mt-4">
          <div className="flex">
            <div className="ml-3">
              <p className="text-sm text-yellow-700">
                Using mock data. {error}
              </p>
            </div>
          </div>
        </div>
      )}

      {/* Today's Summary Cards */}
      <div className="p-6 space-y-4">
        <h2 className="text-xl font-bold text-black mb-4">Today&apos;s Summary</h2>

        <div className="grid grid-cols-2 gap-4">
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
            <Package className="w-8 h-8 text-black mb-2" />
            <p className="text-3xl font-bold text-black">{todayStats.totalDeliveries}</p>
            <p className="text-sm text-gray-600">Total Deliveries</p>
          </div>

          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="w-8 h-8 bg-green-500 rounded-full flex items-center justify-center mb-2">
              <span className="text-white font-bold text-sm">✓</span>
            </div>
            <p className="text-3xl font-bold text-green-700">{todayStats.completed}</p>
            <p className="text-sm text-gray-600">Completed</p>
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <Truck className="w-8 h-8 text-blue-600 mb-2" />
            <p className="text-3xl font-bold text-blue-700">{todayStats.remaining}</p>
            <p className="text-sm text-gray-600">Remaining</p>
          </div>

          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <Clock className="w-8 h-8 text-yellow-600 mb-2" />
            <p className="text-3xl font-bold text-yellow-700">{calculatedETA}</p>
            <p className="text-sm text-gray-600">Next Stop ETA</p>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="mt-8 space-y-3">
          <h2 className="text-xl font-bold text-black mb-4">Quick Actions</h2>

          <Link href="/interfaces/daily-schedule">
            <button className="w-full bg-black text-white py-4 rounded-lg font-semibold hover:bg-gray-800 transition-colors flex items-center justify-center gap-2">
              <MapPin className="w-5 h-5" />
              View Daily Schedule
            </button>
          </Link>

          <Link href="/interfaces/scan-package">
            <button className="w-full bg-white border-2 border-gray-300 text-black py-4 rounded-lg font-semibold hover:bg-gray-50 transition-colors flex items-center justify-center gap-2">
              <CheckCircle className="w-5 h-5" />
              Complete Delivery
            </button>
          </Link>

          <Link href="/interfaces/scan-acceptance">
            <button className="w-full bg-black text-white py-4 rounded-lg font-semibold hover:bg-gray-800 transition-colors flex items-center justify-center gap-2">
              <Package className="w-5 h-5" />
              Scan to Accept
            </button>
          </Link>

          <button
            onClick={handleStartNavigation}
            className="w-full bg-white border-2 border-gray-300 text-black py-4 rounded-lg font-semibold hover:bg-gray-50 transition-colors flex items-center justify-center gap-2"
          >
            <Truck className="w-5 h-5" />
            Start Navigation
          </button>

        </div>

        {/* Next Deliveries Preview */}
        <div className="mt-8">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold text-black">Next Deliveries</h2>
            <Link href="/interfaces/daily-schedule">
              <span className="text-sm text-black hover:text-gray-600 font-semibold">
                View All →
              </span>
            </Link>
          </div>

          {nextDeliveries.length === 0 ? (
            <div className="bg-gray-50 border border-gray-200 rounded-lg p-6 text-center">
              <p className="text-gray-600">No deliveries scheduled</p>
            </div>
          ) : (
            <div className="space-y-3">
              {nextDeliveries.map((delivery) => (
                <div key={delivery.orderId} className="bg-white border border-gray-200 rounded-lg p-4">
                  <div className="flex items-center gap-3 mb-2">
                    <div className="w-8 h-8 bg-black text-white rounded-full flex items-center justify-center font-bold text-sm">
                      {delivery.stopNumber}
                    </div>
                    <div className="flex-1">
                      <p className="font-semibold text-black">{delivery.customerName}</p>
                      <p className="text-xs text-gray-600 flex items-center gap-1">
                        <MapPin className="w-3 h-3" />
                        {delivery.address}
                      </p>
                    </div>
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${getPriorityColor(delivery.priority)}`}>
                      {delivery.priority}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
