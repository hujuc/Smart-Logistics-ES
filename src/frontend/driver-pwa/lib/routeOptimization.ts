/**
 * Route Optimization using Mapbox Optimization API v1
 *
 * This module provides functions to optimize delivery routes using the Mapbox Optimization API.
 * Falls back to a simple nearest-neighbor algorithm if the API is unavailable or fails.
 */

import mapboxgl from 'mapbox-gl';

export interface OptimizationResult {
  optimizedOrder: number[]; // Array of delivery IDs in optimized order
  totalDistance: number; // Total distance in meters
  totalDuration: number; // Total duration in seconds
  success: boolean;
  fallbackUsed: boolean;
}

export interface DeliveryPoint {
  id: number;
  coordinates: [number, number]; // [longitude, latitude]
}

/**
 * Optimize delivery route using Mapbox Optimization API v1
 * @param startLocation The starting point (warehouse location)
 * @param deliveries Array of delivery points with IDs and coordinates
 * @returns OptimizationResult with optimized order
 */
export async function optimizeRouteWithMapbox(
  startLocation: [number, number],
  deliveries: DeliveryPoint[]
): Promise<OptimizationResult> {

  // Check if access token is available
  if (!mapboxgl.accessToken || mapboxgl.accessToken === '') {
    return optimizeWithNearestNeighbor(startLocation, deliveries);
  }

  // Special case: only one delivery
  if (deliveries.length === 1) {
    return {
      optimizedOrder: [deliveries[0].id],
      totalDistance: 0,
      totalDuration: 0,
      success: true,
      fallbackUsed: false
    };
  }

  // Build coordinates array: warehouse location + all delivery locations
  const allCoordinates = [
    startLocation,
    ...deliveries.map(d => d.coordinates)
  ];

  // Validate coordinate count (Optimization API v1 supports 2-12 coordinates)
  if (allCoordinates.length < 3) {
    return {
      optimizedOrder: deliveries.map(d => d.id),
      totalDistance: 0,
      totalDuration: 0,
      success: false,
      fallbackUsed: false
    };
  }

  if (allCoordinates.length > 12) {
    allCoordinates.splice(12);
    deliveries = deliveries.slice(0, 11);
  }

  // Format coordinates string (lon,lat;lon,lat;...)
  const coordinates = allCoordinates
    .map(coord => `${coord[0]},${coord[1]}`)
    .join(';');


  try {
    // Call Mapbox Optimization API v1
    const url = `https://api.mapbox.com/optimized-trips/v1/mapbox/driving/${coordinates}?source=first&destination=any&roundtrip=false&access_token=${mapboxgl.accessToken}`;

    const response = await fetch(url, { method: 'GET' });

    if (!response.ok) {
      // If it's a payment/quota error, fall back to nearest-neighbor
      if (response.status === 402 || response.status === 429) {
        return optimizeWithNearestNeighbor(startLocation, deliveries);
      }

      throw new Error(`API request failed: ${response.status}`);
    }

    const data = await response.json();

    // Check for "NotImplemented" error - API not available in current plan
    if (data.code === 'NotImplemented' || data.message === 'This request is not supported') {
      return optimizeWithNearestNeighbor(startLocation, deliveries);
    }

    if (!data.trips || data.trips.length === 0) {
      if (data.message) {
      }
      return optimizeWithNearestNeighbor(startLocation, deliveries);
    }

    const trip = data.trips[0];

    // Extract optimized order from waypoints
    // The waypoints array contains the optimized order with waypoint_index
    // waypoint_index refers to the original index in our coordinates array
    // Index 0 is the warehouse, so we need to map indices 1+ to delivery IDs
    const optimizedWaypoints = trip.waypoints
      .filter((wp: any) => wp.waypoint_index > 0) // Skip warehouse (index 0)
      .sort((a: any, b: any) => a.waypoint_index - b.waypoint_index);

    const optimizedOrder = optimizedWaypoints.map((wp: any) => {
      const deliveryIndex = wp.waypoint_index - 1; // Adjust for warehouse at index 0
      return deliveries[deliveryIndex].id;
    });


    return {
      optimizedOrder,
      totalDistance: trip.distance,
      totalDuration: trip.duration,
      success: true,
      fallbackUsed: false
    };

  } catch {
    // Fall back to nearest-neighbor algorithm
    return optimizeWithNearestNeighbor(startLocation, deliveries);
  }
}

/**
 * Fallback: Simple greedy nearest-neighbor algorithm
 * @param startLocation The starting point
 * @param deliveries Array of delivery points
 * @returns OptimizationResult with nearest-neighbor order
 */
function optimizeWithNearestNeighbor(
  startLocation: [number, number],
  deliveries: DeliveryPoint[]
): OptimizationResult {

  const optimized: number[] = [];
  const remaining = [...deliveries];
  let currentLocation = startLocation;

  // Simple greedy nearest-neighbor algorithm
  while (remaining.length > 0) {
    let nearestIndex = 0;
    let minDistance = Number.MAX_VALUE;

    remaining.forEach((delivery, index) => {
      const [lng, lat] = delivery.coordinates;
      // Simple Euclidean distance approximation
      const distance = Math.sqrt(
        Math.pow(lng - currentLocation[0], 2) +
        Math.pow(lat - currentLocation[1], 2)
      );
      if (distance < minDistance) {
        minDistance = distance;
        nearestIndex = index;
      }
    });

    const nearest = remaining[nearestIndex];
    optimized.push(nearest.id);
    currentLocation = nearest.coordinates;
    remaining.splice(nearestIndex, 1);
  }


  return {
    optimizedOrder: optimized,
    totalDistance: 0, // We don't calculate actual distance with nearest-neighbor
    totalDuration: 0,
    success: true,
    fallbackUsed: true
  };
}
