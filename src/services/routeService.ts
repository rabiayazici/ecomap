import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE ?? 'http://localhost:4444';
const PYTHON_API_BASE = 'http://localhost:5000';

interface RoutePoint {
  lat: number;
  lon: number;
}

interface VehicleParams {
  type: string;
  fuelType: string;
  weight: number;
  year: number;
}

export const calculateRoute = async (
  startPoint: RoutePoint,
  endPoint: RoutePoint,
  vehicleParams: VehicleParams
) => {
  try {
    const response = await axios.post(`${PYTHON_API_BASE}/api/calculate-eco-route`, {
      startLat: startPoint.lat,
      startLon: startPoint.lon,
      endLat: endPoint.lat,
      endLon: endPoint.lon,
      vehicleType: vehicleParams.type,
      fuelType: vehicleParams.fuelType,
      weight: vehicleParams.weight,
      year: vehicleParams.year
    }, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });

    return response.data;
  } catch (error) {
    console.error('Error calculating route:', error);
    throw error;
  }
};

export const saveRoute = async (route: any) => {
  try {
    const response = await axios.post(`${API_BASE}/api/routes`, route, {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      }
    });
    return response.data;
  } catch (error) {
    console.error('Error saving route:', error);
    throw error;
  }
}; 