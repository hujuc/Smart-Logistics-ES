/**
 * Geocoding utilities using Mapbox Geocoding API
 */

import mapboxgl from 'mapbox-gl';

export interface Coordinates {
  longitude: number;
  latitude: number;
}

/**
 * Geocode an address to coordinates using Mapbox Geocoding API
 */
export async function geocodeAddress(address: string): Promise<Coordinates | null> {
  try {
    const encodedAddress = encodeURIComponent(address);
    const url = `https://api.mapbox.com/geocoding/v5/mapbox.places/${encodedAddress}.json?access_token=${mapboxgl.accessToken}&limit=1&country=US,CA`;

    const response = await fetch(url);
    const data = await response.json();

    if (data.features && data.features.length > 0) {
      const [longitude, latitude] = data.features[0].center;
      return { longitude, latitude };
    }

    return null;
  } catch (error) {
    console.error('Geocoding error:', error);
    return null;
  }
}

/**
 * Build full address string from components
 */
export function buildAddressString(
  addressLine1: string,
  addressLine2: string | null,
  city: string,
  stateOrProvince: string,
  postalCode: string,
  country: string
): string {
  const parts = [
    addressLine1,
    addressLine2,
    city,
    stateOrProvince,
    postalCode,
    country
  ].filter(Boolean);

  return parts.join(', ');
}
