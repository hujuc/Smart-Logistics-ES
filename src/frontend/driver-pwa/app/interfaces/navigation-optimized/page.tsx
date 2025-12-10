"use client";

import { useState, useEffect, useRef, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { MapPin, X, Navigation as NavigationIcon } from 'lucide-react';
import mapboxgl from 'mapbox-gl';
import 'mapbox-gl/dist/mapbox-gl.css';
import { getDailySchedule, DailyScheduleDelivery, getAccessToken } from '@/lib/api';
import { geocodeAddress, buildAddressString, Coordinates } from '@/lib/geocoding';
import { RouteSimulator } from '@/lib/routeSimulation';
import { optimizeRouteWithMapbox } from '@/lib/routeOptimization';
import Alert from '@shared/components/Alert';
import { useAlert } from '@shared/hooks/useAlert';

mapboxgl.accessToken = process.env.NEXT_PUBLIC_MAPBOX_ACCESS_TOKEN || '';

interface DeliveryWithCoords extends DailyScheduleDelivery {
    coordinates: [number, number];
}

function NavigationOptimizedContent() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const { alert, showSuccess, showError, showWarning, closeAlert } = useAlert();
    const deliveryId = searchParams.get('deliveryId');
    const mapContainer = useRef<HTMLDivElement>(null);
    const map = useRef<mapboxgl.Map | null>(null);
    const currentLocationMarker = useRef<mapboxgl.Marker | null>(null);
    const destinationMarker = useRef<mapboxgl.Marker | null>(null);
    const carMarker = useRef<mapboxgl.Marker | null>(null);
    const routeSimulator = useRef<RouteSimulator | null>(null);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [showDeliverModal, setShowDeliverModal] = useState(false);
    const [showExceptionModal, setShowExceptionModal] = useState(false);
    const [simulatedPosition, setSimulatedPosition] = useState<[number, number] | null>(null);
    const [simulationProgress, setSimulationProgress] = useState(0);
    const [selectedPhoto, setSelectedPhoto] = useState<File | null>(null);
    const [photoPreview, setPhotoPreview] = useState<string | null>(null);
    const [isUploading, setIsUploading] = useState(false);
    const [exceptionType, setExceptionType] = useState<string>('CUSTOMER_NOT_AVAILABLE');
    const [exceptionDescription, setExceptionDescription] = useState<string>('');
    const [mapReady, setMapReady] = useState(false);
    const [routeDistance, setRouteDistance] = useState<string>('');
    const [routeDuration, setRouteDuration] = useState<string>('');
    const [arrivalTime, setArrivalTime] = useState<string>('');
    const [initialArrivalTime, setInitialArrivalTime] = useState<string>(''); // Fixed ETA
    const [totalDistance, setTotalDistance] = useState<number>(0); // in meters
    const [totalDuration, setTotalDuration] = useState<number>(0); // in seconds
    const [routeGeometry, setRouteGeometry] = useState<[number, number][]>([]); // Full route coordinates
    const [currentLocation, setCurrentLocation] = useState<[number, number] | null>(null);
    const [warehouseLocation, setWarehouseLocation] = useState<[number, number] | null>(null);
    const [locationError, setLocationError] = useState<string>('');
    const [isLoadingLocation, setIsLoadingLocation] = useState(true);
    const [showLocationWarning, setShowLocationWarning] = useState(false);
    const hasInitialZoom = useRef(false);
    const hasOptimized = useRef(false);
    const [optimizedOrder, setOptimizedOrder] = useState<number[]>([]);
    const [isOptimizing, setIsOptimizing] = useState(false);
    const [optimizationFallbackUsed, setOptimizationFallbackUsed] = useState(false);
    const [isLoadingDeliveries, setIsLoadingDeliveries] = useState(true);
    const [deliveries, setDeliveries] = useState<DeliveryWithCoords[]>([]);
    const [currentDelivery, setCurrentDelivery] = useState<DeliveryWithCoords | null>(null);

    // Load deliveries from API and geocode addresses
    useEffect(() => {
        const loadDeliveries = async () => {
            try {
                setIsLoadingDeliveries(true);
                const schedule = await getDailySchedule();

                console.log('📦 Loaded schedule with', schedule.deliveries.length, 'deliveries');
                console.log('📦 Raw deliveries:', schedule.deliveries);

                const activeStatuses = [
                  'IN_TRANSIT',
                  'CREATED',
                  'READY_FOR_PICKUP',
                  'COLLECTED',
                  'ASSIGNED_TO_SHIPMENT',
                ];
                const pendingDeliveries = schedule.deliveries.filter(d =>
                  activeStatuses.includes(d.status)
                );
                console.log('📦 Pending deliveries:', pendingDeliveries.length);
                console.log('📦 Pending deliveries data:', pendingDeliveries);

                // If no pending deliveries, stop here
                if (pendingDeliveries.length === 0) {
                    console.error('❌ No pending deliveries found!');
                    setDeliveries([]);
                    setIsLoadingDeliveries(false);
                    setIsLoadingLocation(false);
                    setLocationError('No pending deliveries found');
                    return;
                }

                // Geocode warehouse address (starting point) from first delivery
                if (pendingDeliveries.length > 0) {
                    const firstDelivery = pendingDeliveries[0];
                    const warehouseAddress = buildAddressString(
                        firstDelivery.warehouseAddress.addressLine1,
                        firstDelivery.warehouseAddress.addressLine2,
                        firstDelivery.warehouseAddress.city,
                        firstDelivery.warehouseAddress.stateOrProvince,
                        firstDelivery.warehouseAddress.postalCode,
                        firstDelivery.warehouseAddress.country
                    );

                    console.log('🏭 Geocoding warehouse:', warehouseAddress);
                    const warehouseCoords = await geocodeAddress(warehouseAddress);

                    if (warehouseCoords) {
                        const warehouseLngLat: [number, number] = [warehouseCoords.longitude, warehouseCoords.latitude];
                        setWarehouseLocation(warehouseLngLat);
                        setCurrentLocation(warehouseLngLat); // Use warehouse as starting point
                        console.log('✅ Warehouse geocoded:', warehouseCoords);
                    } else {
                        console.warn('⚠️ Failed to geocode warehouse, using fallback location (Aveiro, Portugal)');
                        // Fallback to Aveiro, Portugal coordinates
                        const fallbackLocation: [number, number] = [-8.6537, 40.6443];
                        setWarehouseLocation(fallbackLocation);
                        setCurrentLocation(fallbackLocation);
                    }
                }

                // Geocode all delivery addresses
                const deliveriesWithCoords: DeliveryWithCoords[] = [];

                for (const delivery of pendingDeliveries) {
                    // Use just the shipping address components, not the full address field
                    const address = buildAddressString(
                        delivery.address || '',
                        null,
                        delivery.shippingCity || '',
                        delivery.shippingStateOrProvince || '',
                        delivery.shippingPostalCode || '',
                        delivery.shippingCountry || ''
                    );

                    console.log(`🗺️ Geocoding delivery ${delivery.orderId}: ${address}`);
                    const coords = await geocodeAddress(address);

                    if (coords) {
                        deliveriesWithCoords.push({
                            ...delivery,
                            coordinates: [coords.longitude, coords.latitude]
                        });
                        console.log(`✅ Geocoded delivery ${delivery.orderId}:`, coords);
                    } else {
                        console.warn(`⚠️ Failed to geocode delivery ${delivery.orderId}`);
                    }
                }

                setDeliveries(deliveriesWithCoords);
                console.log(`✅ Successfully geocoded ${deliveriesWithCoords.length}/${pendingDeliveries.length} deliveries`);

                // Set current delivery
                if (deliveryId) {
                    const delivery = deliveriesWithCoords.find(d => d.orderId === parseInt(deliveryId));
                    setCurrentDelivery(delivery || deliveriesWithCoords[0] || null);
                    // Reset refs when changing delivery to allow re-optimization and re-zooming
                    hasOptimized.current = false;
                    hasInitialZoom.current = false;
                    console.log('🔄 Reset optimization and zoom refs for new delivery:', deliveryId);
                } else if (deliveriesWithCoords.length > 0) {
                    // Auto-select first delivery if no deliveryId specified
                    setCurrentDelivery(deliveriesWithCoords[0]);
                    // Add deliveryId to URL without causing reload
                    window.history.replaceState(null, '', `/interfaces/navigation-optimized?deliveryId=${deliveriesWithCoords[0].orderId}`);
                }

                setIsLoadingLocation(false);

            } catch (error) {
                console.error('❌ Error loading deliveries:', error);
                setLocationError('Failed to load deliveries');
                setIsLoadingLocation(false);
            } finally {
                setIsLoadingDeliveries(false);
            }
        };

        loadDeliveries();
    }, [deliveryId]);

    // Optimize delivery route using Mapbox Optimization API v1
    useEffect(() => {
        const optimizeRoute = async () => {
            if (hasOptimized.current || isLoadingLocation || isLoadingDeliveries || deliveries.length === 0 || !currentLocation) return;

            hasOptimized.current = true;
            setIsOptimizing(true);

            console.log('🚀 Starting route optimization with Mapbox Optimization API v1...');
            console.log('Warehouse location (lng, lat):', currentLocation);
            console.log('Deliveries to optimize:', deliveries.length);

            try {
                // Convert deliveries to the format expected by optimizeRouteWithMapbox
                const deliveryPoints = deliveries.map(d => ({
                    id: d.orderId,
                    coordinates: d.coordinates
                }));

                // Call Mapbox Optimization API v1 (with automatic fallback to nearest-neighbor)
                const result = await optimizeRouteWithMapbox(currentLocation, deliveryPoints);

                setOptimizedOrder(result.optimizedOrder);
                setOptimizationFallbackUsed(result.fallbackUsed);

                if (result.fallbackUsed) {
                    console.log('⚠️ Using fallback algorithm (Mapbox API unavailable or quota exceeded)');
                } else {
                    console.log('✅ Route optimized using Mapbox Optimization API v1');
                }

                // Redirect to first optimized delivery
                if (!deliveryId && result.optimizedOrder.length > 0) {
                    router.push(`/interfaces/navigation-optimized?deliveryId=${result.optimizedOrder[0]}`);
                }
            } catch (error) {
                console.error('❌ Error optimizing route:', error);
                // Final fallback: use original order
                setOptimizedOrder(deliveries.map(d => d.orderId));
                setOptimizationFallbackUsed(true);
            } finally {
                setIsOptimizing(false);
            }
        };

        if (!hasOptimized.current && !isLoadingLocation && !isLoadingDeliveries && deliveries.length > 0 && currentLocation) {
            optimizeRoute();
        }
    }, [isLoadingLocation, isLoadingDeliveries, currentLocation, deliveries, deliveryId, router]);

    // Note: We use warehouse location instead of driver's GPS location
    // This ensures routes work properly for US/Canada deliveries

    // Initialize map
    useEffect(() => {
        if (!mapContainer.current || map.current || !currentLocation) {
            if (!mapContainer.current) console.warn('⚠️ Map container ref is null');
            if (map.current) console.log('ℹ️ Map already exists, skipping initialization');
            if (!currentLocation) console.warn('⚠️ Current location not set');
            return;
        }

        console.log('🗺️ Initializing map with center:', currentLocation);

        const newMap = new mapboxgl.Map({
            container: mapContainer.current,
            style: 'mapbox://styles/mapbox/light-v11',
            center: currentLocation,
            zoom: 10
        });

        map.current = newMap;

        newMap.on('load', () => {
            console.log('✅ Map loaded and ready');
            setMapReady(true);
        });

        newMap.addControl(new mapboxgl.NavigationControl(), 'top-right');

        return () => {
            if (map.current) {
                console.log('🧹 Cleaning up map');
                map.current.remove();
                map.current = null;
                setMapReady(false);
            }
        };
    }, [currentLocation]);

    // Update route when location or delivery changes
    useEffect(() => {
        console.log('🔄 Route update effect triggered:', {
            hasMap: !!map.current,
            mapReady,
            currentDeliveryId: currentDelivery?.orderId,
            currentLocation
        });

        if (!map.current || !mapReady || !currentDelivery) {
            console.warn('⚠️ Skipping route update - missing requirements:', {
                hasMap: !!map.current,
                mapReady,
                hasDelivery: !!currentDelivery
            });
            return;
        }

        const updateRoute = async () => {
            console.log('🚀 Starting route update for delivery', currentDelivery?.orderId);
            if (!map.current || !currentDelivery) return;

            if (!map.current.isStyleLoaded()) {
                console.log('⏳ Map style not loaded yet, waiting...');
                map.current.once('styledata', async () => {
                    console.log('✅ Map style loaded, drawing route');
                    await addRouteToMap();
                });
            } else {
                console.log('✅ Map style already loaded, drawing route');
                await addRouteToMap();
            }

            async function addRouteToMap() {
                console.log('📍 Adding route to map:', {
                    deliveryId: currentDelivery?.orderId,
                    from: currentLocation,
                    to: currentDelivery?.coordinates
                });

                if (!map.current || !currentDelivery || !currentLocation) {
                    console.warn('⚠️ Cannot add route - missing data:', {
                        hasMap: !!map.current,
                        hasDelivery: !!currentDelivery,
                        hasLocation: !!currentLocation
                    });
                    return;
                }

                // Check if warehouse and delivery are at the same location
                const sameLocation =
                    Math.abs(currentLocation[0] - currentDelivery.coordinates[0]) < 0.0001 &&
                    Math.abs(currentLocation[1] - currentDelivery.coordinates[1]) < 0.0001;

                if (sameLocation) {
                    console.log('⚠️ Warehouse and delivery are at the same location');
                    // Just show a single marker
                    if (destinationMarker.current) {
                        destinationMarker.current.setLngLat(currentDelivery.coordinates);
                    } else {
                        destinationMarker.current = new mapboxgl.Marker({ color: '#10B981' })
                            .setLngLat(currentDelivery.coordinates)
                            .setPopup(new mapboxgl.Popup().setHTML('<strong>Warehouse & Delivery Location</strong>'))
                            .addTo(map.current);
                    }

                    setRouteDistance('0 km');
                    setRouteDuration('0 min');
                    const now = new Date();
                    setArrivalTime(`${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`);

                    // Center map on the location
                    map.current.flyTo({
                        center: currentDelivery.coordinates,
                        zoom: 15
                    });

                    return;
                }

                // Show small markers for start and end (won't be too prominent during simulation)
                if (destinationMarker.current) {
                    destinationMarker.current.setLngLat(currentDelivery.coordinates);
                } else {
                    // Small red marker at destination
                    const destEl = document.createElement('div');
                    destEl.style.width = '12px';
                    destEl.style.height = '12px';
                    destEl.style.borderRadius = '50%';
                    destEl.style.backgroundColor = '#EF4444';
                    destEl.style.border = '2px solid white';
                    destEl.style.boxShadow = '0 1px 4px rgba(0,0,0,0.3)';

                    destinationMarker.current = new mapboxgl.Marker({ element: destEl })
                        .setLngLat(currentDelivery.coordinates)
                        .addTo(map.current);
                }

                // Fetch route
                try {
                    const query = await fetch(
                        `https://api.mapbox.com/directions/v5/mapbox/driving/${currentLocation[0]},${currentLocation[1]};${currentDelivery.coordinates[0]},${currentDelivery.coordinates[1]}?steps=true&geometries=geojson&overview=full&access_token=${mapboxgl.accessToken}`,
                        { method: 'GET' }
                    );
                    const json = await query.json();
                    const data = json.routes[0];
                    const route = data.geometry.coordinates;

                    // Store route geometry and total distance/duration for accurate remaining distance calculations
                    setRouteGeometry(route);
                    setTotalDistance(data.distance); // meters
                    setTotalDuration(data.duration); // seconds

                    const distanceInKm = (data.distance / 1000).toFixed(1);
                    const durationInMin = Math.round(data.duration / 60);
                    setRouteDistance(`${distanceInKm} km`);
                    setRouteDuration(`${durationInMin} min`);

                    // Calculate and fix the initial arrival time (ETA should not change)
                    const now = new Date();
                    const arrivalDate = new Date(now.getTime() + data.duration * 1000);
                    const hours = arrivalDate.getHours().toString().padStart(2, '0');
                    const minutes = arrivalDate.getMinutes().toString().padStart(2, '0');
                    const etaTime = `${hours}:${minutes}`;
                    setArrivalTime(etaTime);
                    setInitialArrivalTime(etaTime); // Store fixed ETA

                    const routeData = {
                        type: 'Feature' as const,
                        properties: {},
                        geometry: {
                            type: 'LineString' as const,
                            coordinates: route
                        }
                    };

                    const routeSource = map.current.getSource('route') as mapboxgl.GeoJSONSource;
                    if (routeSource) {
                        routeSource.setData(routeData);
                    } else {
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

                    if (!hasInitialZoom.current) {
                        const bounds = new mapboxgl.LngLatBounds();
                        route.forEach((coord: [number, number]) => bounds.extend(coord));
                        map.current!.fitBounds(bounds, { padding: 80 });
                        hasInitialZoom.current = true;
                    }

                    // Start automatic simulation
                    if (route.length > 0) {
                        console.log('🚗 Starting automatic trip simulation');
                        routeSimulator.current = new RouteSimulator(route);
                        routeSimulator.current.setSpeed(2); // 2x speed for nice animation
                        routeSimulator.current.setCallback((position, progress) => {
                            setSimulatedPosition(position);
                            setSimulationProgress(progress);
                        });

                        // Auto-start simulation after 1 second
                        setTimeout(() => {
                            routeSimulator.current?.start();
                        }, 1000);
                    }
                } catch (error) {
                    console.error('Error fetching route:', error);
                }
            }
        };

        updateRoute();
    }, [mapReady, currentLocation, currentDelivery]);

    // Update car marker during simulation
    useEffect(() => {
        if (!map.current || !simulatedPosition) return;

        // Create or update car marker
        if (carMarker.current) {
            carMarker.current.setLngLat(simulatedPosition);
        } else {
            // Create custom car marker element
            const el = document.createElement('div');
            el.style.width = '24px';
            el.style.height = '24px';
            el.style.borderRadius = '50%';
            el.style.backgroundColor = '#3B82F6';
            el.style.border = '3px solid white';
            el.style.boxShadow = '0 2px 8px rgba(0,0,0,0.3)';

            carMarker.current = new mapboxgl.Marker({ element: el })
                .setLngLat(simulatedPosition)
                .addTo(map.current);
        }

        // Smoothly follow the car
        map.current.easeTo({
            center: simulatedPosition,
            duration: 200
        });
    }, [simulatedPosition]);

    // Update distance, time left, and route line based on simulation progress
    useEffect(() => {
        if (totalDistance === 0 || totalDuration === 0 || routeGeometry.length === 0) return;
        if (simulationProgress === 0) return; // Don't update on initial load
        if (!map.current) return;

        // Calculate actual remaining distance using route geometry
        const currentRouteIndex = Math.floor((simulationProgress / 100) * (routeGeometry.length - 1));

        // Get remaining route coordinates from current position to end
        const remainingRoute = routeGeometry.slice(currentRouteIndex);

        // Update the route line to only show remaining path
        const routeSource = map.current.getSource('route') as mapboxgl.GeoJSONSource;
        if (routeSource && remainingRoute.length > 1) {
            routeSource.setData({
                type: 'Feature',
                properties: {},
                geometry: {
                    type: 'LineString',
                    coordinates: remainingRoute
                }
            });
        }

        // Calculate distance from current position to end
        let remainingDistance = 0;
        for (let i = 0; i < remainingRoute.length - 1; i++) {
            const [lon1, lat1] = remainingRoute[i];
            const [lon2, lat2] = remainingRoute[i + 1];

            // Haversine formula for great-circle distance
            const R = 6371000; // Earth's radius in meters
            const φ1 = lat1 * Math.PI / 180;
            const φ2 = lat2 * Math.PI / 180;
            const Δφ = (lat2 - lat1) * Math.PI / 180;
            const Δλ = (lon2 - lon1) * Math.PI / 180;

            const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
                     Math.cos(φ1) * Math.cos(φ2) *
                     Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
            const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            remainingDistance += R * c;
        }

        // Calculate remaining time based on average speed from total route
        const averageSpeed = totalDistance / totalDuration; // meters per second
        const remainingDuration = remainingDistance / averageSpeed; // seconds

        // Update displayed values
        const distanceInKm = (remainingDistance / 1000).toFixed(1);
        const durationInMin = Math.round(remainingDuration / 60);

        setRouteDistance(`${distanceInKm} km`);
        setRouteDuration(`${durationInMin} min`);

        // Keep the original arrival time (ETA should not change)
        // Don't recalculate arrival time
    }, [simulationProgress, totalDistance, totalDuration, routeGeometry]);

    const handleCenterLocation = () => {
        if (map.current && mapReady) {
            map.current.flyTo({
                center: currentLocation,
                zoom: 16,
                duration: 1000
            });
        }
    };

    const compressImage = (file: File): Promise<File> => {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const img = new Image();
                img.onload = () => {
                    const canvas = document.createElement('canvas');
                    const ctx = canvas.getContext('2d');
                    if (!ctx) {
                        reject(new Error('Could not get canvas context'));
                        return;
                    }

                    // Max dimensions
                    const maxWidth = 1200;
                    const maxHeight = 1200;
                    let width = img.width;
                    let height = img.height;

                    // Calculate new dimensions
                    if (width > height) {
                        if (width > maxWidth) {
                            height = (height * maxWidth) / width;
                            width = maxWidth;
                        }
                    } else {
                        if (height > maxHeight) {
                            width = (width * maxHeight) / height;
                            height = maxHeight;
                        }
                    }

                    canvas.width = width;
                    canvas.height = height;

                    // Draw and compress
                    ctx.drawImage(img, 0, 0, width, height);
                    canvas.toBlob(
                        (blob) => {
                            if (blob) {
                                const compressedFile = new File([blob], file.name, {
                                    type: 'image/jpeg',
                                    lastModified: Date.now(),
                                });
                                console.log(`📸 Compressed image: ${(file.size / 1024).toFixed(1)}KB → ${(blob.size / 1024).toFixed(1)}KB`);
                                resolve(compressedFile);
                            } else {
                                reject(new Error('Could not compress image'));
                            }
                        },
                        'image/jpeg',
                        0.8 // 80% quality
                    );
                };
                img.onerror = () => reject(new Error('Could not load image'));
                img.src = e.target?.result as string;
            };
            reader.onerror = () => reject(new Error('Could not read file'));
            reader.readAsDataURL(file);
        });
    };

    const handlePhotoSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (file) {
            try {
                // Compress the image
                const compressedFile = await compressImage(file);
                setSelectedPhoto(compressedFile);

                // Create preview
                const reader = new FileReader();
                reader.onloadend = () => {
                    setPhotoPreview(reader.result as string);
                };
                reader.readAsDataURL(compressedFile);
            } catch (error) {
                console.error('Failed to compress image:', error);
                alert('Failed to process image. Please try again.');
            }
        }
    };

    const handleRedirectToDailySchedule = (title: string, message: string) => {
        // Show success message
        showSuccess(title, message);

        // Small delay to let user see success message
        setTimeout(() => {
            router.push('/interfaces/daily-schedule');
        }, 1500);
    };

    const handleFinishPOD = async () => {
        if (!currentDelivery || !selectedPhoto) {
            showWarning('Photo Required', 'Please take a photo first');
            return;
        }

        setIsUploading(true);

        try {
            const formData = new FormData();
            formData.append('photo', selectedPhoto);

            const token = getAccessToken();
            const response = await fetch(`http://localhost/api/driver/orders/${currentDelivery.orderId}/deliver`, {
                method: 'POST',
                headers: {
                    ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
                credentials: 'include',
                body: formData,
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ error: 'Upload failed' }));
                throw new Error(errorData.error || `Upload failed: ${response.status}`);
            }

            const result = await response.json();
            console.log('✅ Delivery completed:', result);

            setShowDeliverModal(false);
            setSelectedPhoto(null);
            setPhotoPreview(null);

            handleRedirectToDailySchedule(
                'Delivery Completed',
                `Order ${currentDelivery.orderReference || currentDelivery.orderId} has been marked as DELIVERED.`
            );

        } catch (error) {
            console.error('❌ Failed to complete delivery:', error);
            showError('Delivery Failed', 'Failed to complete delivery: ' + (error instanceof Error ? error.message : 'Unknown error'));
        } finally {
            setIsUploading(false);
        }
    };

    const handleReportException = async () => {
        if (!currentDelivery) {
            showError('Error', 'No delivery selected');
            return;
        }

        if (!exceptionDescription.trim()) {
            showWarning('Description Required', 'Please provide a description of the issue');
            return;
        }

        setIsUploading(true);

        try {
            const formData = new FormData();
            formData.append('exceptionType', exceptionType);
            formData.append('description', exceptionDescription);

            const token = getAccessToken();
            const response = await fetch(`http://localhost/api/driver/orders/${currentDelivery.orderId}/exception`, {
                method: 'POST',
                headers: {
                    ...(token ? { Authorization: `Bearer ${token}` } : {}),
                },
                credentials: 'include',
                body: formData,
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ error: 'Failed to report exception' }));
                throw new Error(errorData.error || `Failed: ${response.status}`);
            }

            const result = await response.json();
            console.log('✅ Exception reported:', result);

            setShowExceptionModal(false);
            setExceptionDescription('');
            setExceptionType('CUSTOMER_NOT_AVAILABLE');

            handleRedirectToDailySchedule(
                'Exception Reported',
                `Order ${currentDelivery.orderReference || currentDelivery.orderId} has been flagged for review.`
            );

        } catch (error) {
            console.error('❌ Failed to report exception:', error);
            showError('Exception Failed', 'Failed to report exception: ' + (error instanceof Error ? error.message : 'Unknown error'));
        } finally {
            setIsUploading(false);
        }
    };

    if (isLoadingLocation || isLoadingDeliveries) {
        console.log('🔄 Loading state:', { isLoadingLocation, isLoadingDeliveries });
        return (
            <div className="absolute inset-0 bg-white z-50 flex flex-col items-center justify-center">
                <div className="relative">
                    <div className="w-16 h-16 border-4 border-blue-200 border-t-blue-600 rounded-full animate-spin"></div>
                    <MapPin className="w-8 h-8 text-blue-600 absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2" />
                </div>
                <p className="mt-6 text-lg font-semibold text-gray-800">Loading navigation...</p>
            </div>
        );
    }

    console.log('📍 Render state:', {
        currentDelivery: currentDelivery?.orderId,
        currentLocation,
        warehouseLocation,
        mapReady,
        deliveries: deliveries.length
    });

    if (!currentDelivery) {
        return (
            <div className="flex items-center justify-center h-screen bg-gray-50">
                <div className="text-center p-8">
                    <div className="bg-gray-100 rounded-full p-6 mb-4 inline-block">
                        <MapPin className="w-16 h-16 text-gray-400" />
                    </div>
                    <h2 className="text-xl font-bold text-gray-800 mb-2">No Deliveries Available</h2>
                    <p className="text-gray-600 mb-6">
                        {locationError || 'No pending deliveries found'}
                    </p>
                    <Link href="/interfaces/daily-schedule">
                        <button className="px-6 py-3 bg-black text-white rounded-lg font-semibold hover:bg-gray-800 transition-colors">
                            Back to Schedule
                        </button>
                    </Link>
                </div>
            </div>
        );
    }

    return (
        <div className="relative h-screen w-full">
            {/* Alert Component */}
            <Alert
                isOpen={alert.isOpen}
                onClose={closeAlert}
                title={alert.title}
                message={alert.message}
                type={alert.type}
            />

            {/* Map Container */}
            <div ref={mapContainer} className="absolute inset-0 w-full h-full" />

            {/* Optimization Indicator */}
            {isOptimizing && (
                <div className="absolute top-24 left-4 right-4 bg-blue-100 border-l-4 border-blue-500 text-blue-800 px-4 py-3 rounded-lg shadow-lg z-20">
                    <div className="flex items-start gap-3">
                        <div className="w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin flex-shrink-0 mt-0.5"></div>
                        <div>
                            <p className="font-semibold text-sm">Optimizing route</p>
                            <p className="text-sm mt-1">Calculating the best delivery order...</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Fallback Algorithm Warning */}
            {optimizationFallbackUsed && !isOptimizing && (
                <div className="absolute top-24 left-4 right-4 bg-blue-100 border-l-4 border-blue-500 text-blue-800 px-4 py-3 rounded-lg shadow-lg z-20">
                    <div className="flex items-start gap-3">
                        <svg className="w-5 h-5 text-blue-600 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <div>
                            <p className="font-semibold text-sm">Using nearest-neighbor routing</p>
                            <p className="text-sm mt-1">Optimized routing requires Mapbox paid plan</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Location Warning */}
            {showLocationWarning && locationError && (
                <div className="absolute top-20 left-4 right-4 bg-yellow-100 border-l-4 border-yellow-500 text-yellow-800 px-4 py-3 rounded-lg shadow-lg z-20">
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
                            <span className="text-sm font-semibold text-black">Optimized Navigation</span>
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
                            <p className="text-2xl font-bold text-black">{routeDuration || '--'}</p>
                            <p className="text-xs text-gray-600">ETA</p>
                        </div>
                        <div className="h-12 w-px bg-gray-300" />
                        <div className="text-center">
                            <p className="text-2xl font-bold text-black">{routeDistance || '--'}</p>
                            <p className="text-xs text-gray-600">Distance</p>
                        </div>
                        <div className="h-12 w-px bg-gray-300" />
                        <div className="text-center">
                            <p className="text-2xl font-bold text-black">{initialArrivalTime || arrivalTime || '--'}</p>
                            <p className="text-xs text-gray-600">Arrival</p>
                        </div>
                    </div>
                </div>
                <div className="flex gap-3">
                    <button
                        onClick={() => setShowDeliverModal(true)}
                        className="flex-1 bg-black text-white py-4 rounded-xl font-bold text-lg hover:bg-gray-800 transition-colors shadow-lg"
                    >
                        Deliver
                    </button>
                    <button
                        onClick={() => setShowExceptionModal(true)}
                        className="flex-1 bg-purple-400 text-white py-4 rounded-xl font-bold text-lg hover:bg-purple-500 transition-colors shadow-lg"
                    >
                        Report Issue
                    </button>
                </div>
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
                <div className="fixed inset-0 bg-black/50 flex items-end justify-center z-50">
                    <div className="bg-white rounded-t-3xl w-full max-w-2xl p-6 pb-8">
                        <div className="w-12 h-1 bg-gray-300 rounded-full mx-auto mb-6" />
                        <h2 className="text-2xl font-bold text-black mb-2">Proof of Delivery</h2>
                        <p className="text-sm text-gray-600 mb-6">Complete delivery for {currentDelivery.customerName}</p>

                        {/* Photo Preview or Capture Button */}
                        <div className="mb-6">
                            {photoPreview ? (
                                <div className="relative">
                                    <img src={photoPreview} alt="POD Photo" className="w-full h-64 object-cover rounded-lg" />
                                    <button
                                        onClick={() => {
                                            setSelectedPhoto(null);
                                            setPhotoPreview(null);
                                        }}
                                        className="absolute top-2 right-2 bg-red-500 text-white p-2 rounded-full"
                                    >
                                        <X className="w-4 h-4" />
                                    </button>
                                </div>
                            ) : (
                                <div>
                                    <input
                                        ref={fileInputRef}
                                        type="file"
                                        accept="image/*"
                                        capture="environment"
                                        onChange={handlePhotoSelect}
                                        className="hidden"
                                    />
                                    <button
                                        onClick={() => fileInputRef.current?.click()}
                                        className="w-full bg-gray-100 border-2 border-dashed border-gray-300 rounded-lg p-8 hover:bg-gray-50 transition-colors flex flex-col items-center gap-2"
                                    >
                                        <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0020.07 7H21a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
                                        </svg>
                                        <span className="text-gray-600 font-semibold">Take Delivery Photo</span>
                                    </button>
                                </div>
                            )}
                        </div>

                        <div className="flex gap-3">
                            <button
                                onClick={() => {
                                    setShowDeliverModal(false);
                                    setSelectedPhoto(null);
                                    setPhotoPreview(null);
                                }}
                                className="w-10 h-10 flex items-center justify-center bg-white border-2 border-gray-300 text-black rounded-lg hover:bg-gray-50"
                                disabled={isUploading}
                            >
                                <X className="w-6 h-6" />
                            </button>
                            <button
                                onClick={handleFinishPOD}
                                disabled={!selectedPhoto || isUploading}
                                className="flex-1 bg-green-600 text-white py-3 rounded-xl font-bold hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                            >
                                {isUploading ? (
                                    <>
                                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                        <span>Uploading...</span>
                                    </>
                                ) : (
                                    <>
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                                        </svg>
                                        <span>Complete Delivery</span>
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Exception Modal */}
            {showExceptionModal && (
                <div className="fixed inset-0 bg-black/50 flex items-end justify-center z-50">
                    <div className="bg-white rounded-t-3xl w-full max-w-2xl p-6 pb-8 max-h-[80vh] overflow-y-auto">
                        <div className="w-12 h-1 bg-gray-300 rounded-full mx-auto mb-6" />
                        <h2 className="text-2xl font-bold text-black mb-2">Report Delivery Issue</h2>
                        <p className="text-sm text-gray-600 mb-6">Report an issue for {currentDelivery.customerName}</p>

                        {/* Exception Type Selection */}
                        <div className="mb-6">
                            <label className="block text-sm font-semibold text-black mb-2">Issue Type</label>
                            <select
                                value={exceptionType}
                                onChange={(e) => setExceptionType(e.target.value)}
                                className="w-full px-4 py-3 bg-white border-2 border-gray-200 rounded-xl text-black font-medium focus:border-purple-400 focus:outline-none focus:ring-2 focus:ring-purple-200 transition-all appearance-none cursor-pointer hover:border-gray-300"
                                style={{
                                    backgroundImage: `url("data:image/svg+xml;charset=UTF-8,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='none' stroke='currentColor' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3e%3cpolyline points='6 9 12 15 18 9'%3e%3c/polyline%3e%3c/svg%3e")`,
                                    backgroundRepeat: 'no-repeat',
                                    backgroundPosition: 'right 1rem center',
                                    backgroundSize: '1.5em 1.5em',
                                    paddingRight: '3rem'
                                }}
                            >
                                <option value="CUSTOMER_NOT_AVAILABLE">Customer Not Available</option>
                                <option value="WRONG_ADDRESS">Wrong Address</option>
                                <option value="ACCESS_DENIED">Access Denied</option>
                                <option value="DAMAGED">Package Damaged</option>
                                <option value="WEATHER">Weather Conditions</option>
                                <option value="OTHER">Other</option>
                            </select>
                        </div>

                        {/* Description */}
                        <div className="mb-6">
                            <label className="block text-sm font-semibold text-black mb-2">Description</label>
                            <textarea
                                value={exceptionDescription}
                                onChange={(e) => setExceptionDescription(e.target.value)}
                                placeholder="Provide details about the issue..."
                                rows={4}
                                className="w-full px-4 py-3 bg-white border-2 border-gray-200 rounded-xl focus:border-purple-400 focus:outline-none focus:ring-2 focus:ring-purple-200 resize-none transition-all"
                            />
                        </div>

                        <div className="flex gap-3">
                            <button
                                onClick={() => {
                                    setShowExceptionModal(false);
                                    setExceptionDescription('');
                                    setExceptionType('CUSTOMER_NOT_AVAILABLE');
                                }}
                                className="w-10 h-10 flex items-center justify-center bg-white border-2 border-gray-300 text-black rounded-lg hover:bg-gray-50"
                                disabled={isUploading}
                            >
                                <X className="w-6 h-6" />
                            </button>
                            <button
                                onClick={handleReportException}
                                disabled={!exceptionDescription.trim() || isUploading}
                                className="flex-1 bg-purple-400 text-white py-3 rounded-xl font-bold hover:bg-purple-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                            >
                                {isUploading ? (
                                    <>
                                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                        <span>Submitting...</span>
                                    </>
                                ) : (
                                    <>
                                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                        </svg>
                                        <span>Submit Issue</span>
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}

export default function NavigationOptimized() {
    return (
        <Suspense fallback={<div className="flex items-center justify-center h-screen">Loading...</div>}>
            <NavigationOptimizedContent />
        </Suspense>
    );
}
