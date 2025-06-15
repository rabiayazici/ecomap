import React, { useState } from 'react';
import { calculateRoute } from '../services/routeService';

const RoutePlanningPage: React.FC = () => {
  const [startPoint, setStartPoint] = useState({ lat: 0, lon: 0 });
  const [endPoint, setEndPoint] = useState({ lat: 0, lon: 0 });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleCalculateRoute = async () => {
    try {
      setLoading(true);
      setError(null);

      const vehicleParams = {
        type: 'medium',
        fuelType: 'petrol',
        weight: 1500,
        year: 2020
      };

      const result = await calculateRoute(startPoint, endPoint, vehicleParams);
      console.log('Route calculated:', result);
      
      // Handle the result here (e.g., display on map)
      
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to calculate route');
      console.error('Error calculating route:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-4">Plan Route</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold mb-2">Start Point</h2>
            <div className="grid grid-cols-2 gap-2">
              <input
                type="number"
                placeholder="Latitude"
                value={startPoint.lat}
                onChange={(e) => setStartPoint({ ...startPoint, lat: parseFloat(e.target.value) })}
                className="border p-2 rounded"
              />
              <input
                type="number"
                placeholder="Longitude"
                value={startPoint.lon}
                onChange={(e) => setStartPoint({ ...startPoint, lon: parseFloat(e.target.value) })}
                className="border p-2 rounded"
              />
            </div>
          </div>
          
          <div>
            <h2 className="text-lg font-semibold mb-2">End Point</h2>
            <div className="grid grid-cols-2 gap-2">
              <input
                type="number"
                placeholder="Latitude"
                value={endPoint.lat}
                onChange={(e) => setEndPoint({ ...endPoint, lat: parseFloat(e.target.value) })}
                className="border p-2 rounded"
              />
              <input
                type="number"
                placeholder="Longitude"
                value={endPoint.lon}
                onChange={(e) => setEndPoint({ ...endPoint, lon: parseFloat(e.target.value) })}
                className="border p-2 rounded"
              />
            </div>
          </div>
          
          <button
            onClick={handleCalculateRoute}
            disabled={loading}
            className="w-full bg-green-600 text-white py-2 px-4 rounded hover:bg-green-700 disabled:bg-gray-400"
          >
            {loading ? 'Calculating...' : 'Calculate Route'}
          </button>
          
          {error && (
            <div className="text-red-500 mt-2">
              {error}
            </div>
          )}
        </div>
        
        <div className="border-2 border-dashed border-gray-300 p-4 rounded-lg">
          <h2 className="text-xl font-semibold mb-2">Map Area</h2>
          {/* Add your map component here */}
          <p className="text-center py-10 text-gray-500">Map will be displayed here</p>
        </div>
      </div>
    </div>
  );
};

export default RoutePlanningPage; 