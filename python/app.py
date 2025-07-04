from flask import Flask, request, jsonify
from flask_cors import CORS
from eco_route import main as calculate_eco_route
import logging

app = Flask(__name__)

CORS(app, 
    origins="http://localhost:5173",
    supports_credentials=True,
    allow_headers=["Content-Type", "Authorization", "Accept", "Origin", "X-Requested-With"])
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.route('/api/calculate-eco-route', methods=['POST'])
def calculate_route():
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'No data provided'}), 400
        
        # Extract coordinates and vehicle parameters
        try:
            start_lat = float(data['startLat'])
            start_lon = float(data['startLon'])
            end_lat = float(data['endLat'])
            end_lon = float(data['endLon'])
        except (KeyError, ValueError) as e:
            return jsonify({'error': f'Invalid coordinates: {str(e)}'}), 400
        
        # Get vehicle parameters
        vehicle_params = {
            'type': data['vehicleType'],
            'fuel_type': data['fuelType'],
            'weight': float(data['weight']),
            'year': int(data['year'])
        }
        
        # Calculate routes
        shortest_route, eco_route = calculate_eco_route(
            start_lat, start_lon,
            end_lat, end_lon,
            vehicle_params
        )
        
        if shortest_route is None or eco_route is None:
            return jsonify({
                'error': 'Could not calculate routes'
            }), 400
            
        return jsonify({
            'shortest_route': {
                'coordinates': [[lon, lat] for lat, lon in shortest_route]
            },
            'eco_route': {
                'coordinates': [[lon, lat] for lat, lon, _ in eco_route]
            }
        })
        
    except Exception as e:
        logger.error(f"Error calculating route: {str(e)}")
        return jsonify({
            'error': str(e)
        }), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True) 