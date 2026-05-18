#!/bin/sh
set -eu

SERVER_URL="http://keycloak:8080"
REALM="${KEYCLOAK_REALM:-ae-realm}"
ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
BACKEND_ADMIN_CLIENT_ID="${KEYCLOAK_BACKEND_ADMIN_CLIENT_ID:-travel-backend-admin}"
SERVICE_ACCOUNT_USERNAME="service-account-${BACKEND_ADMIN_CLIENT_ID}"

echo "Waiting for Keycloak..."
until /opt/keycloak/bin/kcadm.sh config credentials \
  --server "$SERVER_URL" \
  --realm master \
  --user "$ADMIN_USER" \
  --password "$ADMIN_PASSWORD" >/dev/null 2>&1; do
  sleep 2
done

echo "Waiting for realm ${REALM}..."
until /opt/keycloak/bin/kcadm.sh get "realms/${REALM}" >/dev/null 2>&1; do
  sleep 2
done

echo "Configuring backend service-account permissions..."
for role in manage-users query-users view-users query-clients view-clients view-realm; do
  /opt/keycloak/bin/kcadm.sh add-roles \
    -r "$REALM" \
    --uusername "$SERVICE_ACCOUNT_USERNAME" \
    --cclientid realm-management \
    --rolename "$role" >/dev/null 2>&1 || true
done

echo "Keycloak realm configured."
