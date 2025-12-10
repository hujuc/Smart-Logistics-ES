"use client";

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { Truck, Package, MapPin, Clock, Bell, CheckCircle, XCircle, Settings } from 'lucide-react';
import { getDashboard, DashboardResponse, getPendingShipments, acceptShipment, PendingShipment } from '@/lib/api';
import { useUser } from '@/shared/hooks/use-user';

export default function DriverHome() {
  const user = useUser();
  const [dashboardData, setDashboardData] = useState<DashboardResponse | null>(null);
  const [pendingShipments, setPendingShipments] = useState<PendingShipment[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [acceptingShipmentId, setAcceptingShipmentId] = useState<number | null>(null);

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
      alert('Shipment accepted successfully!');
      // Remove from pending list
      setPendingShipments(prev => prev.filter(s => s.id !== shipmentId));
      // Refresh dashboard
      const dashData = await getDashboard().catch(() => null);
      if (dashData) setDashboardData(dashData);
    } catch (err) {
      console.error('Failed to accept shipment:', err);
      alert('Failed to accept shipment: ' + (err instanceof Error ? err.message : 'Unknown error'));
    } finally {
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
    switch(priority) {
      case 'High': return 'bg-red-100 text-red-700';
      case 'Medium': return 'bg-yellow-100 text-yellow-700';
      case 'Low': return 'bg-gray-100 text-gray-700';
      default: return 'bg-gray-100 text-gray-700';
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
        {/* Header */}
        <div className="bg-black text-white p-6">
          <div className="flex items-center gap-2 mb-6">
            <Package className="h-6 w-6 text-white" />
            <span className="text-lg font-bold text-white">SLMS</span>
          </div>
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="text-3xl font-bold">Welcome Back</h1>
              <p className="text-gray-300 text-sm mt-1">
                {user ? `${user.firstName} ${user.lastName}` : 'Driver'} • Driver
              </p>
            </div>
            <div className="flex items-center gap-4">
              <Link href="/notifications" className="relative">
                <Bell className="w-6 h-6" />
                <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                  2
                </span>
              </Link>
              <Link href="/settings">
                <Settings className="w-6 h-6 hover:text-gray-300 cursor-pointer" />
              </Link>
            </div>
          </div>
          <p className="text-gray-400 text-sm">
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

        {/* Pending Shipments - Awaiting Acceptance */}
        {pendingShipments.length > 0 && (
          <div className="p-6 pb-0">
            <div className="bg-orange-50 border-2 border-orange-300 rounded-lg p-4">
              <h2 className="text-lg font-bold text-orange-900 mb-3 flex items-center gap-2">
                <Bell className="w-5 h-5" />
                Shipments Awaiting Your Acceptance
              </h2>
              <div className="space-y-3">
                {pendingShipments.map((shipment) => (
                  <div key={shipment.id} className="bg-white rounded-lg p-4 border border-orange-200">
                    <div className="flex items-start justify-between mb-3">
                      <div>
                        <p className="font-bold text-black text-lg">{shipment.shipmentCode}</p>
                        <p className="text-sm text-gray-600">{shipment.carrierName}</p>
                        <p className="text-sm text-gray-600">{shipment.totalOrders} order(s)</p>
                      </div>
                      <span className="bg-orange-100 text-orange-800 text-xs font-semibold px-3 py-1 rounded-full">
                        Pending
                      </span>
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleAcceptShipment(shipment.id)}
                        disabled={acceptingShipmentId === shipment.id}
                        className="flex-1 bg-green-600 text-white py-3 rounded-lg font-semibold hover:bg-green-700 transition-colors flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                      >
                        <CheckCircle className="w-5 h-5" />
                        {acceptingShipmentId === shipment.id ? 'Accepting...' : 'Accept Shipment'}
                      </button>
                      <button
                        className="flex-1 bg-gray-200 text-gray-700 py-3 rounded-lg font-semibold hover:bg-gray-300 transition-colors flex items-center justify-center gap-2"
                      >
                        <XCircle className="w-5 h-5" />
                        Reject
                      </button>
                    </div>
                  </div>
                ))}
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
              <p className="text-3xl font-bold text-yellow-700">{todayStats.nextStopETA}</p>
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
                <Package className="w-5 h-5" />
                Scan Package
              </button>
            </Link>

            <Link href="/interfaces/navigation">
              <button className="w-full bg-white border-2 border-gray-300 text-black py-4 rounded-lg font-semibold hover:bg-gray-50 transition-colors flex items-center justify-center gap-2">
                <Truck className="w-5 h-5" />
                Start Navigation
              </button>
            </Link>

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
