# RideShare Backend (Spring Boot + MongoDB Atlas)

A RideShare backend implemented with Spring Boot 3.2.x and Java 21. It uses MongoDB for persistence and JWT-based stateless authentication, with a layered application structure:
- DTO -> Controller -> Service -> Repository
- Global validation and exception handling
- Role-based flows for `ROLE_USER` and `ROLE_DRIVER`

## Tech Stack
- Java 21 (source level)
- Spring Boot 3.2.12
- Spring Web, Security, Data MongoDB, Validation
- JWT via `jjwt` (HS256)
- Environment management via `.env` and `java-dotenv`
- Build: Maven Wrapper (`./mvnw`)

## Project Structure
```
src/
  main/
    java/org/example/rideshare/
      RideshareApplication.java
      config/ (security, JWT, filters)
      controller/ (Auth, Ride, Driver)
      dto/ (Auth and Ride requests/responses)
      exception/ (custom + global handler)
      model/ (User, Ride)
      repository/ (UserRepository, RideRepository)
      service/ (UserService, RideService)
    resources/
      application.properties
      templates/, static/ (reserved)
```

## Configuration
The app reads configuration from environment variables with sensible defaults. Use a `.env` file in the project root for local development.

`.env` sample:
```
MONGODB_URI=mongodb+srv://<user>:<pass>@<cluster>/<db>?retryWrites=true&w=majority
MONGODB_DATABASE=rideshare
JWT_SECRET=change-me-to-a-strong-32+char-secret
JWT_EXP_SECONDS=36000
SERVER_PORT=8081
```

`src/main/resources/application.properties` (key entries):
```
server.port=${SERVER_PORT:8081}
spring.application.name=RideShare
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/rideshare}
spring.data.mongodb.database=${MONGODB_DATABASE:rideshare}
security.jwt.secret=${JWT_SECRET:local-dev-secret-32-characters-minimum-123456}
security.jwt.expiration-seconds=${JWT_EXP_SECONDS:36000}
```

Notes:
- Ensure `JWT_SECRET` is at least 32 characters.
- Atlas SRV URIs are supported.
- `.env` is auto-loaded by `RideshareApplication` at startup.

## How to Run
Prerequisites:
- JDK 21+ installed (runtime may show 23/25 but source is 21).
- Internet access to connect to MongoDB Atlas.

Build and run locally:
```sh
# From project root
./mvnw clean package
./mvnw spring-boot:run
```
Or run the built jar:
```sh
java -jar target/rideshare-0.0.1-SNAPSHOT.jar
```
Server starts at `http://localhost:8081`.

### Quick E2E Test Script
With the server running, execute:
```sh
chmod +x scripts/test-e2e.sh
./scripts/test-e2e.sh
```
This registers users, logs in, requests a ride, the driver accepts and completes, and the user lists rides.

## Accounts & Roles
- `ROLE_USER`: can register/login, request rides, list own rides.
- `ROLE_DRIVER`: can login, list pending requests, accept rides, complete rides.

## API Summary
All responses are JSON. On validation or errors, a global handler returns:
```
{
  "error": "BadRequest | NotFound | Unauthorized | Forbidden",
  "message": "details",
  "timestamp": "ISO-8601"
}
```

Authentication:
- Register: `POST /api/auth/register`
- Login: `POST /api/auth/login` → returns `{ token }`
- Use `Authorization: Bearer <token>` for protected endpoints.

User endpoints:
- `POST /api/v1/rides` — request a ride (ROLE_USER)
- `GET /api/v1/user/rides` — list my rides (ROLE_USER)

Driver endpoints:
- `GET /api/v1/driver/rides/requests` — list pending ride requests (ROLE_DRIVER)
- `POST /api/v1/driver/rides/{rideId}/accept` — accept a pending ride (ROLE_DRIVER)
- `POST /api/v1/rides/{rideId}/complete` — complete an accepted ride (ROLE_DRIVER)

### DTOs
Register:
```json
{
  "username": "alice",
  "password": "StrongPass123",
  "role": "ROLE_USER"
}
```
Login:
```json
{
  "username": "alice",
  "password": "StrongPass123"
}
```
Create Ride:
```json
{
  "pickupLocation": "A",
  "dropLocation": "B"
}
```

## Step-by-Step Testing (cURL)
Run these with the server running.

1) Register users
```sh
curl -s -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"StrongPass123","role":"ROLE_USER"}'

curl -s -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"driver2","password":"StrongPass123","role":"ROLE_DRIVER"}'
```

2) Login and capture tokens
```sh
USER_TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"StrongPass123"}' | jq -r .token)

DRIVER_TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"driver2","password":"StrongPass123"}' | jq -r .token)
```

3) User requests a ride
```sh
RIDE_ID=$(curl -s -X POST http://localhost:8081/api/v1/rides \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pickupLocation":"Point A","dropLocation":"Point B"}' | jq -r .id)
```

4) Driver lists pending requests
```sh
curl -s -H "Authorization: Bearer $DRIVER_TOKEN" \
  http://localhost:8081/api/v1/driver/rides/requests | jq .
```

5) Driver accepts ride
```sh
curl -s -X POST \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  http://localhost:8081/api/v1/driver/rides/$RIDE_ID/accept | jq .
```

6) Driver completes ride
```sh
curl -s -X POST \
  -H "Authorization: Bearer $DRIVER_TOKEN" \
  http://localhost:8081/api/v1/rides/$RIDE_ID/complete | jq .
```

7) User lists own rides
```sh
curl -s -H "Authorization: Bearer $USER_TOKEN" \
  http://localhost:8081/api/v1/user/rides | jq .
```

Tips:
- If you don't have `jq`, replace `| jq -r .token` with manual parsing or install via Homebrew: `brew install jq`.
- If registering existing usernames, you'll receive a 400 error; login instead.

## Postman Collection
- Import `postman/RideShare.postman_collection.json` into Postman.
- Import environment `postman/RideShare.postman_environment.json` and select it.
- Run in order:
  1. Auth → Register USER, Register DRIVER
  2. Auth → Login USER (sets `USER_TOKEN`), Login DRIVER (sets `DRIVER_TOKEN`)
  3. User → Request Ride (sets `RIDE_ID`)
  4. Driver → List Pending Requests, Accept Ride, Complete Ride
  5. User → List My Rides
- Tokens and `RIDE_ID` are saved into environment automatically via tests.

### Run via Collection Runner
- Open the collection, click "Run".
- Ensure `RideShare Local` environment is selected.
- Manually run items in order (Auth → User → Driver) or create a Postman folder-based run if preferred.

## Development Notes
- This project originated as a course assignment; the repository history preserves that context.
- Validation via Jakarta: `@NotBlank`, `@Size`, `@Pattern` on DTOs.
- BCrypt for password hashing.
- JWT carries `username` and `role`; HS256 using UTF-8 bytes of secret.
- Security is stateless; only specific endpoints are permitted without auth (register, login).

## Troubleshooting
- Port already in use: change `SERVER_PORT` in `.env`.
- Mongo connection errors: check `MONGODB_URI`, internet connectivity, Atlas IP allowlist.
- 401/403 on endpoints: ensure `Authorization: Bearer <token>` header and correct role.
- Token issues: rotate `JWT_SECRET` and re-login to get a fresh token.

## License
This project is for educational purposes. No explicit license provided.
