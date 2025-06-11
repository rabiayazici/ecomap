# Car API Documentation

## Create a New Car

**Endpoint:** `POST /api/cars`

**Description:** Creates a new car for the authenticated user.

**Request Body:**

```json
{
  "name": "Car Name",
  "model": "Car Model",
  "engine_type": "Engine Type",
  "year": 2020,
  "fuel_type": "Fuel Type",
  "engine_displacement": 1.8,
  "transmission": "Transmission Type",
  "drive_type": "Drive Type",
  "fuelConsumption": 7.5
}
```

**Response:**

*   **200 OK:** Car created successfully.
*   **400 Bad Request:** Invalid input.
*   **401 Unauthorized:** User not authenticated.

## Get Car by ID

**Endpoint:** `GET /api/cars/{id}`

**Description:** Retrieves a car by its ID.

**Path Parameters:**

*   `id`: The ID of the car to retrieve.

**Response:**

*   **200 OK:** Car found.
*   **404 Not Found:** Car not found.

## Get Cars by User ID

**Endpoint:** `GET /api/cars/user/{userId}`

**Description:** Retrieves all cars belonging to a specific user.

**Path Parameters:**

*   `userId`: The ID of the user whose cars to retrieve.

**Response:**

*   **200 OK:** Cars found.

## Update Car

**Endpoint:** `PUT /api/cars/{id}`

**Description:** Updates a car by its ID.

**Path Parameters:**

*   `id`: The ID of the car to update.

**Request Body:**

```json
{
  "name": "Updated Car Name",
  "model": "Updated Car Model",
  "engine_type": "Updated Engine Type",
  "year": 2021,
  "fuel_type": "Updated Fuel Type",
  "engine_displacement": 2.0,
  "transmission": "Updated Transmission Type",
  "drive_type": "Updated Drive Type",
  "fuelConsumption": 8.0
}
```

**Response:**

*   **200 OK:** Car updated successfully.
*   **404 Not Found:** Car not found.

## Delete Car

**Endpoint:** `DELETE /api/cars/{id}`

**Description:** Deletes a car by its ID.

**Path Parameters:**

*   `id`: The ID of the car to delete.

**Response:**

*   **200 OK:** Car deleted successfully.
*   **404 Not Found:** Car not found. 