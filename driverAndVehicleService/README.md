# RideLink — Driver & Vehicle Service (`driverAndVehicleService`)

## 1. Overview & Service Responsibility
The **Driver & Vehicle Service** is responsible for managing driver operational data — completely decoupled from driver identity, which is owned by the **Account Service**.

- **Owner Service**: Driver & Vehicle Service
- **Runtime**: Java 17+ / 21+ / 23+, Spring Boot 4.1.1 / 3.x
- **Port**: `8081` (Account Service runs on `8080`)
- **Database**: MongoDB Atlas (`ridelink_driver_db` — strictly isolated database per microservice)
- **API Demonstration**: Shared Postman Collection (`RideLink_Driver_Service_Postman_Collection.json`)

---

## 2. Interservice Contract (For Ride Management Service)

### `GET /drivers/available?area={area}&minCapacity={minCapacity}&vehicleType={vehicleType}`
Called by the **Ride Management Service** during the ride dispatch and driver assignment workflow.

#### Query Parameters:
| Parameter | Type | Required | Description |
|---|---|---|---|
| `area` | `String` | No | Operational service area or current location area (e.g., `"Colombo"`, `"Kandy"`) |
| `minCapacity` | `Integer` | No | Minimum vehicle passenger seating capacity (e.g., `4`) |
| `vehicleType` | `String` | No | Type of vehicle (`SEDAN`, `SUV`, `VAN`, `HATCHBACK`, `MOTORCYCLE`, `THREE_WHEELER`) |

#### Response Shape (`200 OK`):
```json
[
  {
    "driverId": "66f43e5c9b1a8d0012e34567",
    "accountId": "66f43d1a9b1a8d0012e34560",
    "serviceArea": "Colombo",
    "availability": "AVAILABLE",
    "vehicle": {
      "make": "Toyota",
      "model": "Prius",
      "licensePlate": "WP-CAB-1234",
      "capacity": 4,
      "vehicleType": "SEDAN",
      "color": "White"
    },
    "currentLocation": {
      "latitude": 6.9271,
      "longitude": 79.8612,
      "areaName": "Colombo 03",
      "updatedAt": "2026-09-25T08:30:00Z"
    }
  }
]
```

---

## 3. Endpoints Summary

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/drivers/{accountId}/profile` | Create driver operational profile | `DRIVER` (owner) or `ADMIN` |
| `GET` | `/drivers/{driverId}` | Get driver operational profile by Driver ID | Authenticated |
| `GET` | `/drivers/account/{accountId}` | Get driver operational profile by Account ID | Authenticated |
| `PUT` | `/drivers/{driverId}/vehicle` | Register or update vehicle details | `DRIVER` (owner) or `ADMIN` |
| `PATCH` | `/drivers/{driverId}/availability` | Update availability (`OFFLINE`, `AVAILABLE`, `BUSY`) | `DRIVER` (owner) or `ADMIN` |
| `PATCH` | `/drivers/{driverId}/location` | Update simulated GPS location and area | `DRIVER` (owner) or `ADMIN` |
| `GET` | `/drivers/available` | Retrieve eligible available drivers for dispatch | Authenticated / Interservice |

---

## 4. Demonstrable Negative Scenarios (Viva & Rubric)

1. **No Available Driver** (`GET /drivers/available?area=NonExistentArea`):
   - Returns empty list `[]` (`200 OK`). Ride Management can catch this to trigger a `NO_DRIVERS_AVAILABLE` rejection in the ride state machine.
2. **Invalid Status Transition / Missing Vehicle** (`PATCH /drivers/{driverId}/availability`):
   - Setting a driver to `AVAILABLE` without a registered vehicle or when `SUSPENDED` returns `400 Bad Request`:
     ```json
     {
       "timestamp": "2026-09-25T08:50:00Z",
       "status": 400,
       "error": "Bad Request",
       "message": "Cannot set driver availability to AVAILABLE without an assigned vehicle",
       "path": "/drivers/123/availability"
     }
     ```
3. **Unauthorized Access / Modification** (`PUT /drivers/{driverId}/vehicle`):
   - A driver attempting to modify another driver's vehicle or location returns `403 Forbidden`:
     ```json
     {
       "timestamp": "2026-09-25T08:50:00Z",
       "status": 403,
       "error": "Forbidden",
       "message": "You are not authorized to modify another driver's data",
       "path": "/drivers/driver-1/vehicle"
     }
     ```
4. **Duplicate Driver Profile** (`POST /drivers/{accountId}/profile`):
   - Registering a second operational profile for the same account ID returns `409 Conflict`.
5. **Driver Not Found** (`GET /drivers/{driverId}`):
   - Returns `404 Not Found`.

---

## 5. Running the Microservice
```bash
# Compile and run unit tests
mvn test -pl driverAndVehicleService

# Run the driver service
mvn spring-boot:run -pl driverAndVehicleService
```
The service will start on `http://localhost:8081`.
