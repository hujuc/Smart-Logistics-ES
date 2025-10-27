"use client";

import Link from 'next/link';
import { Truck, Package, MapPin, Clock, Bell } from 'lucide-react';

export default function DriverHome() {
  // Mock data
  const todayStats = {
    totalDeliveries: 25,
    completed: 8,
    remaining: 17,
    nextStopETA: '15 min'
  };

  return (
      <div className="min-h-screen bg-white">
        {/* Header */}
        <div className="bg-black text-white p-6">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h1 className="text-3xl font-bold">Welcome Back</h1>
              <p className="text-gray-300 text-sm mt-1">Carlos Silva • Driver</p>
            </div>
            <div className="relative">
              <Bell className="w-6 h-6" />
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
              3
            </span>
            </div>
          </div>
          <p className="text-gray-400 text-sm">Tuesday, October 14, 2025</p>
        </div>

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

            <Link href="/">
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

            <div className="space-y-3">
              <div className="bg-white border border-gray-200 rounded-lg p-4">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-8 h-8 bg-black text-white rounded-full flex items-center justify-center font-bold text-sm">
                    1
                  </div>
                  <div className="flex-1">
                    <p className="font-semibold text-black">João Silva</p>
                    <p className="text-xs text-gray-600 flex items-center gap-1">
                      <MapPin className="w-3 h-3" />
                      123 Rua Street, Aveiro
                    </p>
                  </div>
                  <span className="px-2 py-1 bg-red-100 text-red-700 text-xs font-medium rounded-full">
                  High
                </span>
                </div>
              </div>

              <div className="bg-white border border-gray-200 rounded-lg p-4">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-8 h-8 bg-black text-white rounded-full flex items-center justify-center font-bold text-sm">
                    2
                  </div>
                  <div className="flex-1">
                    <p className="font-semibold text-black">Maria Santos</p>
                    <p className="text-xs text-gray-600 flex items-center gap-1">
                      <MapPin className="w-3 h-3" />
                      456 Ave Street, Aveiro
                    </p>
                  </div>
                  <span className="px-2 py-1 bg-yellow-100 text-yellow-700 text-xs font-medium rounded-full">
                  Medium
                </span>
                </div>
              </div>

              <div className="bg-white border border-gray-200 rounded-lg p-4">
                <div className="flex items-center gap-3 mb-2">
                  <div className="w-8 h-8 bg-black text-white rounded-full flex items-center justify-center font-bold text-sm">
                    3
                  </div>
                  <div className="flex-1">
                    <p className="font-semibold text-black">Carlos Ferreira</p>
                    <p className="text-xs text-gray-600 flex items-center gap-1">
                      <MapPin className="w-3 h-3" />
                      789 Main Street, Aveiro
                    </p>
                  </div>
                  <span className="px-2 py-1 bg-gray-100 text-gray-700 text-xs font-medium rounded-full">
                  Low
                </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
  );
}