"use client";

import { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { MapPin, X, Navigation as NavigationIcon } from 'lucide-react';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';

mapboxgl.accessToken = 'pk.eyJ1IjoidWdvb28iLCJhIjoiY21ndGdndmZtMDNsNTJqczQ2d3hnbmlyaiJ9.oRyPS2ZviBFuAcSwrVKkcg';

type Delivery = {
    id: number;
    customerName: string;
    address: string;
    coordinates: [number, number];
    eta: string;
    distance: string;
};

export default function DriverNavigation() {
    const searchParams = useSearchParams();
    const deliveryId = searchParams.get('deliveryId');
    const mapContainer = useRef<HTMLDivElement>(null);
    const map = useRef<mapboxgl.Map | null>(null);
    const [showDeliverModal, setShowDeliverModal] = useState(false);
    const [mapReady, setMapReady] = useState(false);

    const deliveries: Record<number, Delivery> = {
        1: {
            id: 1,
            customerName: 'João Jacinto de Magalhães',
            address: 'Av. João Jacinto de Magalhães, Aveiro',
            coordinates: [-8.6537, 40.6443],
            eta: '23 min',
            distance: '2.0 km'
        },
        2: {
            id: 2,
            customerName: 'Maria Santos',
            address: '456 Ave Street, Aveiro',
            coordinates: [-8.6500, 40.6400],
            eta: '18 min',
            distance: '1.5 km'
        }
    };

    const currentDelivery = deliveries[Number(deliveryId)] || deliveries[1];
    const currentLocation: [number, number] = [-8.6600, 40.6500];

    useEffect(() => {
        if (!mapContainer.current) return;

        // Remove existing map
        if (map.current) {
            map.current.remove();
            map.current = null;
        }

        // Initialize map
        const newMap = new mapboxgl.Map({
            container: mapContainer.current,
            style: 'mapbox://styles/mapbox/streets-v12',
            center: currentLocation,
            zoom: 13
        });

        map.current = newMap;

        newMap.on('load', () => {
            if (!map.current) return;

            // Add current location marker
            new mapboxgl.Marker({ color: '#3B82F6' })
                .setLngLat(currentLocation)
                .addTo(map.current);

            // Add destination marker
            new mapboxgl.Marker({ color: '#EF4444' })
                .setLngLat(currentDelivery.coordinates)
                .addTo(map.current);

            // Add route line
            map.current.addSource('route', {
                type: 'geojson',
                data: {
                    type: 'Feature',
                    properties: {},
                    geometry: {
                        type: 'LineString',
                        coordinates: [currentLocation, currentDelivery.coordinates]
                    }
                }
            });

            map.current.addLayer({
                id: 'route',
                type: 'line',
                source: 'route',
                layout: {
                    'line-join': 'round',
                    'line-cap': 'round'
                },
                paint: {
                    'line-color': '#3B82F6',
                    'line-width': 4
                }
            });

            // Fit bounds
            const bounds = new mapboxgl.LngLatBounds();
            bounds.extend(currentLocation);
            bounds.extend(currentDelivery.coordinates);
            map.current.fitBounds(bounds, { padding: 80 });

            setMapReady(true);
        });

        // Add navigation controls
        newMap.addControl(new mapboxgl.NavigationControl(), 'top-right');

        return () => {
            if (map.current) {
                map.current.remove();
                map.current = null;
            }
        };
    }, [deliveryId, currentDelivery]);

    const handleCenterLocation = () => {
        if (map.current && mapReady) {
            map.current.flyTo({
                center: currentLocation,
                zoom: 16,
                duration: 1000
            });
        }
    };

    return (
        <div className="relative h-screen w-full">
            {/* Map Container */}
            <div ref={mapContainer} className="absolute inset-0 w-full h-full" />

            {/* Top Bar */}
            <div className="absolute top-0 left-0 right-0 bg-white shadow-lg z-10">
                <div className="p-4">
                    <div className="flex items-center justify-between mb-3">
                        <Link href="/interfaces/daily-schedule">
                            <button className="text-black hover:text-gray-600 transition-colors">
                                <X className="w-6 h-6" />
                            </button>
                        </Link>
                        <div className="flex items-center gap-2 bg-gray-100 px-3 py-1 rounded-full">
                            <NavigationIcon className="w-4 h-4 text-blue-600" />
                            <span className="text-sm font-semibold text-black">Navigation</span>
                        </div>
                        <div className="w-6" />
                    </div>
                    <div className="bg-gray-50 rounded-lg p-3 border border-gray-200">
                        <div className="flex items-start gap-3">
                            <MapPin className="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
                            <div className="flex-1">
                                <p className="font-semibold text-black text-sm">
                                    {currentDelivery.customerName}
                                </p>
                                <p className="text-xs text-gray-600 mt-1">
                                    {currentDelivery.address}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Bottom Card */}
            <div className="absolute bottom-0 left-0 right-0 bg-white rounded-t-3xl shadow-2xl z-10 p-6">
                <div className="flex justify-center items-center mb-4">
                    <div className="flex items-center gap-4">
                        <div className="text-center">
                            <p className="text-2xl font-bold text-black">{currentDelivery.eta}</p>
                            <p className="text-xs text-gray-600">ETA</p>
                        </div>
                        <div className="h-12 w-px bg-gray-300" />
                        <div className="text-center">
                            <p className="text-2xl font-bold text-black">{currentDelivery.distance}</p>
                            <p className="text-xs text-gray-600">Distance</p>
                        </div>
                        <div className="h-12 w-px bg-gray-300" />
                        <div className="text-center">
                            <p className="text-2xl font-bold text-black">16:05</p>
                            <p className="text-xs text-gray-600">Arrival</p>
                        </div>
                    </div>
                </div>
                <button
                    onClick={() => setShowDeliverModal(true)}
                    className="w-full bg-blue-600 text-white py-4 rounded-xl font-bold text-lg hover:bg-blue-700 transition-colors shadow-lg"
                >
                    Deliver
                </button>
            </div>

            {/* Center Location Button */}
            <button
                onClick={handleCenterLocation}
                disabled={!mapReady}
                className="absolute left-4 top-40 bg-white p-3 rounded-full shadow-lg z-10 hover:bg-gray-50 transition-colors disabled:opacity-50"
            >
                <NavigationIcon className="w-6 h-6 text-black" />
            </button>

            {/* Deliver Modal */}
            {showDeliverModal && (
                <div className="fixed inset-0 bg-black/50 flex items-end justify-center z-50" style={{ zIndex: 9999 }}>
                    <div className="bg-white rounded-t-3xl w-full max-w-2xl p-6 pb-8" onClick={(e) => e.stopPropagation()}>
                        <div className="w-12 h-1 bg-gray-300 rounded-full mx-auto mb-6" />

                        <h2 className="text-2xl font-bold text-black mb-2">Proof of Delivery</h2>
                        <p className="text-sm text-gray-600 mb-6">You are 5 meters away from the delivery location</p>

                        <div className="space-y-3 mb-6">
                            <button className="w-full bg-white border-2 border-black rounded-xl p-4 flex items-center justify-between hover:bg-gray-50 transition-colors">
                                <span className="font-semibold text-black">Take a delivery photo</span>
                                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0020.07 7H21a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                                </svg>
                            </button>

                            <button className="w-full bg-white border-2 border-black rounded-xl p-4 flex items-center justify-between hover:bg-gray-50 transition-colors">
                                <span className="font-semibold text-black">Collect client&#39;s Signature</span>
                                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                                </svg>
                            </button>

                            <button className="w-full bg-white border-2 border-black rounded-xl p-4 flex items-center justify-between hover:bg-gray-50 transition-colors">
                                <span className="font-semibold text-black">Add Delivery Note</span>
                                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                </svg>
                            </button>

                            <button className="w-full bg-white border-2 border-red-500 rounded-xl p-4 flex items-center justify-between hover:bg-red-50 transition-colors">
                                <span className="font-semibold text-red-600">Report Delivery Issue</span>
                                <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4v2m0-12a9 9 0 110 18 9 9 0 010-18z" />
                                </svg>
                            </button>
                        </div>

                        <div className="flex gap-3">
                            <button
                                onClick={() => setShowDeliverModal(false)}
                                className="w-10 h-10 flex items-center justify-center bg-white border-2 border-gray-300 text-black rounded-lg hover:bg-gray-50 transition-colors"
                            >
                                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                                </svg>
                            </button>
                            <button
                                className="flex-1 bg-purple-500 text-white py-3 rounded-xl font-bold hover:bg-purple-600 transition-colors"
                            >
                                Finish POD
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}