"use client";

import { useState, useMemo } from 'react';
import { MapPin, Package, Phone, Navigation, ChevronDown, ChevronUp, Clock, AlertCircle, ArrowLeft } from 'lucide-react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';

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

export default function DriverDailySchedule() {
  const router = useRouter();
  const [expandedOrder, setExpandedOrder] = useState<number | null>(null);
  const [sortBy, setSortBy] = useState<'route' | 'time' | 'priority'>('route');

  // Mock data baseado nos requisitos
  const deliveryStats = {
    totalStops: 25,
    totalPackages: 48,
    estimatedTime: '23:45',
    distance: '87 km'
  };

  const deliveriesData: Delivery[] = [
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
    },
    {
      id: 4,
      stopNumber: 4,
      customerName: 'João Roldão',
      address: '1011 Rua de Espinho, Aveiro, CA 94211',
      packageCount: 30,
      priority: 'Medium',
      timeWindow: '08:00-11:00',
      status: 'Pending',
      specialInstructions: 'If not home, leave with neighbor at 1013.',
      phone: '+351 912 345 678',
      weight: '50.1 kg'
    }
  ];

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
  }, [sortBy]);

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
    router.push(`/interfaces/navigation?deliveryId=${deliveryId}`);
  };

  const handleViewMap = () => {
    // Navigate to first delivery
    const firstDelivery = sortedDeliveries[0];
    if (firstDelivery) {
      router.push(`/interfaces/navigation?deliveryId=${firstDelivery.id}`);
    }
  };

  const handleGoToNextOrder = () => {
    // Find first pending delivery
    const nextDelivery = sortedDeliveries.find(d => d.status === 'Pending');
    if (nextDelivery) {
      router.push(`/interfaces/navigation?deliveryId=${nextDelivery.id}`);
    }
  };

  return (
      <div className="min-h-screen bg-white pb-20">
        {/* Header */}
        <div className="bg-black text-white p-4">
          <h1 className="text-2xl font-bold">Daily Schedule</h1>
          <p className="text-gray-300 text-sm mt-1">Carlos Silva • Driver</p>
          <div className="absolute top-4 right-4">
            <Link href="/" className="flex items-center gap-1 text-white hover:text-gray-300">
              <ArrowLeft className="w-5 h-5" />
              Back
            </Link>
          </div>
        </div>

        {/* Route Summary */}
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
              <p className="text-lg font-semibold text-black">{deliveryStats.estimatedTime}</p>
            </div>
            <div>
              <p className="text-gray-600 text-sm">Distance</p>
              <p className="text-lg font-semibold text-black">{deliveryStats.distance}</p>
            </div>
          </div>

          <button 
            onClick={handleViewMap}
            className="w-full bg-black text-white py-3 rounded-lg font-semibold hover:bg-gray-800 transition-colors"
          >
            View Map
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

        {/* Bottom Navigation */}
        <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 p-4">
          <button 
            onClick={handleGoToNextOrder}
            className="w-full bg-black text-white py-4 rounded-lg font-semibold text-lg hover:bg-gray-800 transition-colors"
          >
            Go to Next Order
          </button>
        </div>
      </div>
  );
}