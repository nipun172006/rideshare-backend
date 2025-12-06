#!/usr/bin/env zsh
set -euo pipefail

BASE="http://localhost:8081"
USER="alice"
USER_PASS="StrongPass123"
DRIVER="driver2"
DRIVER_PASS="StrongPass123"

jq --version >/dev/null 2>&1 || { echo "Please install jq (brew install jq)"; exit 1; }

echo "[1/7] Registering users (may error if exist)"
curl -s -X POST "$BASE/api/auth/register" -H "Content-Type: application/json" \
  -d '{"username":"'$USER'","password":"'$USER_PASS'","role":"ROLE_USER"}' | jq . || true
curl -s -X POST "$BASE/api/auth/register" -H "Content-Type: application/json" \
  -d '{"username":"'$DRIVER'","password":"'$DRIVER_PASS'","role":"ROLE_DRIVER"}' | jq . || true

echo "[2/7] Logging in"
USER_TOKEN=$(curl -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" \
  -d '{"username":"'$USER'","password":"'$USER_PASS'"}' | jq -r .token)
DRIVER_TOKEN=$(curl -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" \
  -d '{"username":"'$DRIVER'","password":"'$DRIVER_PASS'"}' | jq -r .token)

if [[ -z "$USER_TOKEN" || -z "$DRIVER_TOKEN" ]]; then
  echo "Login failed; tokens missing"; exit 1
fi

echo "[3/7] Requesting a ride"
RIDE_ID=$(curl -s -X POST "$BASE/api/v1/rides" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pickupLocation":"Point A","dropLocation":"Point B"}' | jq -r .id)

if [[ -z "$RIDE_ID" ]]; then
  echo "Ride request failed; RIDE_ID missing"; exit 1
fi

echo "[4/7] Driver lists pending"
curl -s -H "Authorization: Bearer $DRIVER_TOKEN" "$BASE/api/v1/driver/rides/requests" | jq .

echo "[5/7] Driver accepts ride $RIDE_ID"
curl -s -X POST -H "Authorization: Bearer $DRIVER_TOKEN" "$BASE/api/v1/driver/rides/$RIDE_ID/accept" | jq .

echo "[6/7] Driver completes ride $RIDE_ID"
curl -s -X POST -H "Authorization: Bearer $DRIVER_TOKEN" "$BASE/api/v1/rides/$RIDE_ID/complete" | jq .

echo "[7/7] User lists own rides"
curl -s -H "Authorization: Bearer $USER_TOKEN" "$BASE/api/v1/user/rides" | jq .

echo "Done. Ride ID: $RIDE_ID"
