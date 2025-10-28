"use client";

import { useState, useEffect, useRef, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
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

function NavigationContent() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const deliveryId = searchParams.get('deliveryId');
    const mapContainer = useRef<HTMLDivElement>(null);
    const map = useRef<mapboxgl.Map | null>(null);
    const currentLocationMarker = useRef<mapboxgl.Marker | null>(null);
    const destinationMarker = useRef<mapboxgl.Marker | null>(null);
    const [showDeliverModal, setShowDeliverModal] = useState(false);
    const [mapReady, setMapReady] = useState(false);
    const [routeDistance, setRouteDistance] = useState<string>('');
    const [routeDuration, setRouteDuration] = useState<string>('');
    const [arrivalTime, setArrivalTime] = useState<string>('');
    const [currentLocation, setCurrentLocation] = useState<[number, number]>([-8.6600, 40.6500]); // Default fallback location
    const [locationError, setLocationError] = useState<string>('');
    const hasInitialZoom = useRef(false); // Track if initial zoom has been done

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

    // Get user's current location
    useEffect(() => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    setCurrentLocation([position.coords.longitude, position.coords.latitude]);
                    setLocationError('');
                },
                (error) => {
                    console.error('Error getting location:', error);
                    setLocationError('Could not get your location. Using default location.');
                }
            );

            // Watch position for real-time updates
            const watchId = navigator.geolocation.watchPosition(
                (position) => {
                    setCurrentLocation([position.coords.longitude, position.coords.latitude]);
                },
                (error) => {
                    console.error('Error watching location:', error);
                }
            );

            return () => {
                navigator.geolocation.clearWatch(watchId);
            };
        } else {
            setLocationError('Geolocation is not supported by your browser.');
        }
    }, []);

    // Initialize map only once
    useEffect(() => {
        if (!mapContainer.current || map.current) return;

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
            setMapReady(true);
        });

        // Add navigation controls
        newMap.addControl(new mapboxgl.NavigationControl(), 'top-right');

        return () => {
            if (map.current) {
                map.current.remove();
                map.current = null;
            }
            if (currentLocationMarker.current) {
                currentLocationMarker.current.remove();
                currentLocationMarker.current = null;
            }
            if (destinationMarker.current) {
                destinationMarker.current.remove();
                destinationMarker.current = null;
            }
        };
    }, []);

    // Update route when location or delivery changes
    useEffect(() => {
        if (!map.current || !mapReady) return;

        const updateRoute = async () => {
            if (!map.current) return;

            // Wait for style to be fully loaded
            if (!map.current.isStyleLoaded()) {
                map.current.once('styledata', async () => {
                    await addRouteToMap();
                });
            } else {
                await addRouteToMap();
            }

            async function addRouteToMap() {
                if (!map.current) return;

                // Update or create current location marker
                if (currentLocationMarker.current) {
                    currentLocationMarker.current.setLngLat(currentLocation);
                } else {
                    currentLocationMarker.current = new mapboxgl.Marker({ color: '#3B82F6' })
                        .setLngLat(currentLocation)
                        .addTo(map.current);
                }

                // Update or create destination marker
                if (destinationMarker.current) {
                    destinationMarker.current.setLngLat(currentDelivery.coordinates);
                } else {
                    destinationMarker.current = new mapboxgl.Marker({ color: '#EF4444' })
                        .setLngLat(currentDelivery.coordinates)
                        .addTo(map.current);
                }

                // Fetch route from Mapbox Directions API
                try {
                    const query = await fetch(
                        `https://api.mapbox.com/directions/v5/mapbox/driving/${currentLocation[0]},${currentLocation[1]};${currentDelivery.coordinates[0]},${currentDelivery.coordinates[1]}?steps=true&geometries=geojson&access_token=${mapboxgl.accessToken}`,
                        { method: 'GET' }
                    );
                    const json = await query.json();
                    const data = json.routes[0];
                    const route = data.geometry.coordinates;

                    // Extract and format distance and duration
                    const distanceInKm = (data.distance / 1000).toFixed(1);
                    const durationInMin = Math.round(data.duration / 60);
                    setRouteDistance(`${distanceInKm} km`);
                    setRouteDuration(`${durationInMin} min`);

                    // Calculate arrival time
                    const now = new Date();
                    const arrivalDate = new Date(now.getTime() + data.duration * 1000);
                    const hours = arrivalDate.getHours().toString().padStart(2, '0');
                    const minutes = arrivalDate.getMinutes().toString().padStart(2, '0');
                    setArrivalTime(`${hours}:${minutes}`);

                    const routeData = {
                        type: 'Feature' as const,
                        properties: {},
                        geometry: {
                            type: 'LineString' as const,
                            coordinates: route
                        }
                    };

                    // Update existing source or create new one
                    const routeSource = map.current.getSource('route') as mapboxgl.GeoJSONSource;
                    if (routeSource) {
                        // Just update the data without removing/recreating
                        routeSource.setData(routeData);
                    } else {
                        // First time: create source and layer
                        map.current!.addSource('route', {
                            type: 'geojson',
                            data: routeData
                        });

                        map.current!.addLayer({
                            id: 'route',
                            type: 'line',
                            source: 'route',
                            layout: {
                                'line-join': 'round',
                                'line-cap': 'round'
                            },
                            paint: {
                                'line-color': '#3B82F6',
                                'line-width': 5,
                                'line-opacity': 0.75
                            }
                        });
                    }

                // Fit bounds to show entire route only on first load
                if (!hasInitialZoom.current) {
                    const bounds = new mapboxgl.LngLatBounds();
                    route.forEach((coord: [number, number]) => bounds.extend(coord));
                    map.current!.fitBounds(bounds, { padding: 80 });
                    hasInitialZoom.current = true;
                }
            } catch (error) {
                console.error('Error fetching route:', error);
                // Fallback to straight line and default values if API fails
                setRouteDistance(currentDelivery.distance);
                setRouteDuration(currentDelivery.eta);
                
                // Calculate fallback arrival time based on ETA
                const etaMinutes = parseInt(currentDelivery.eta);
                const now = new Date();
                const arrivalDate = new Date(now.getTime() + etaMinutes * 60 * 1000);
                const hours = arrivalDate.getHours().toString().padStart(2, '0');
                const minutes = arrivalDate.getMinutes().toString().padStart(2, '0');
                setArrivalTime(`${hours}:${minutes}`);
                
                const fallbackRouteData = {
                    type: 'Feature' as const,
                    properties: {},
                    geometry: {
                        type: 'LineString' as const,
                        coordinates: [currentLocation, currentDelivery.coordinates]
                    }
                };

                // Update existing source or create new one
                const routeSource = map.current!.getSource('route') as mapboxgl.GeoJSONSource;
                if (routeSource) {
                    // Just update the data without removing/recreating
                    routeSource.setData(fallbackRouteData);
                } else {
                    // First time: create source and layer
                    map.current!.addSource('route', {
                        type: 'geojson',
                        data: fallbackRouteData
                    });

                    map.current!.addLayer({
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
                }

                // Fit bounds to show entire route only on first load
                if (!hasInitialZoom.current) {
                    const bounds = new mapboxgl.LngLatBounds();
                    bounds.extend(currentLocation);
                    bounds.extend(currentDelivery.coordinates);
                    map.current.fitBounds(bounds, { padding: 80 });
                    hasInitialZoom.current = true;
                }
                }
            }
        };

        updateRoute();
    }, [mapReady, currentLocation, currentDelivery, deliveryId]);

    const handleCenterLocation = () => {
        if (map.current && mapReady) {
            map.current.flyTo({
                center: currentLocation,
                zoom: 16,
                duration: 1000
            });
        }
    };

    const handleFinishPOD = () => {
        // Get all delivery IDs and sort them
        const deliveryIds = Object.keys(deliveries).map(Number).sort((a, b) => a - b);
        const currentId = Number(deliveryId) || 1; // Default to 1 if deliveryId is null/undefined
        const currentIndex = deliveryIds.indexOf(currentId);
        
        console.log('Current delivery ID:', currentId);
        console.log('All delivery IDs:', deliveryIds);
        console.log('Current index:', currentIndex);
        console.log('Total deliveries:', deliveryIds.length);
        
        // Check if there's a next delivery
        if (currentIndex !== -1 && currentIndex < deliveryIds.length - 1) {
            // Go to next delivery
            const nextDeliveryId = deliveryIds[currentIndex + 1];
            console.log('Going to next delivery:', nextDeliveryId);
            router.push(`/interfaces/navigation?deliveryId=${nextDeliveryId}`);
            setShowDeliverModal(false);
        } else {
            // No more deliveries, go back to daily schedule
            console.log('No more deliveries, going back to daily schedule');
            router.push('/interfaces/daily-schedule');
        }
    };

    return (
        <div className="relative h-screen w-full">
            {/* Map Container */}
            <div ref={mapContainer} className="absolute inset-0 w-full h-full" />

            {/* Location Error Message */}
            {locationError && (
                <div className="absolute top-20 left-4 right-4 bg-yellow-100 border border-yellow-400 text-yellow-800 px-4 py-3 rounded-lg z-20">
                    <p className="text-sm">{locationError}</p>
                </div>
            )}

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
                            <p className="text-2xl font-bold text-black">{routeDuration || currentDelivery.eta}</p>
                            <p className="text-xs text-gray-600">ETA</p>
                        </div>
                        <div className="h-12 w-px bg-gray-300" />
                        <div className="text-center">
                            <p className="text-2xl font-bold text-black">{routeDistance || currentDelivery.distance}</p>
                            <p className="text-xs text-gray-600">Distance</p>
                        </div>
                        <div className="h-12 w-px bg-gray-300" />
                        <div className="text-center">
                            <p className="text-2xl font-bold text-black">{arrivalTime || '16:05'}</p>
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
                                onClick={handleFinishPOD}
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

export default function DriverNavigation() {
    return (
        <Suspense fallback={<div className="flex items-center justify-center h-screen">Loading...</div>}>
            <NavigationContent />
        </Suspense>
    );
}
