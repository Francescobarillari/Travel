#!/bin/sh
set -eu

SERVER_URL="http://keycloak:8080"
REALM="${KEYCLOAK_REALM:-ae-realm}"
ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin}"
BACKEND_ADMIN_CLIENT_ID="${KEYCLOAK_BACKEND_ADMIN_CLIENT_ID:-travel-backend-admin}"
SERVICE_ACCOUNT_USERNAME="service-account-${BACKEND_ADMIN_CLIENT_ID}"
MAX_ATTEMPTS="${KEYCLOAK_CONFIG_MAX_ATTEMPTS:-90}"

echo "Waiting for Keycloak..."
attempt=1
while ! /opt/keycloak/bin/kcadm.sh config credentials \
    --server "$SERVER_URL" \
    --realm master \
    --user "$ADMIN_USER" \
    --password "$ADMIN_PASSWORD" >/dev/null 2>&1; do
  if [ "$attempt" -ge "$MAX_ATTEMPTS" ]; then
    echo "Keycloak admin login failed after ${MAX_ATTEMPTS} attempts."
    echo "Check: docker logs keycloak"
    /opt/keycloak/bin/kcadm.sh config credentials \
      --server "$SERVER_URL" \
      --realm master \
      --user "$ADMIN_USER" \
      --password "$ADMIN_PASSWORD" || true
    exit 1
  fi
  echo "Keycloak not ready yet (${attempt}/${MAX_ATTEMPTS})..."
  attempt=$((attempt + 1))
  sleep 2
done

echo "Waiting for realm ${REALM}..."
attempt=1
while ! /opt/keycloak/bin/kcadm.sh get "realms/${REALM}" >/dev/null 2>&1; do
  if [ "$attempt" -ge "$MAX_ATTEMPTS" ]; then
    echo "Realm ${REALM} was not found after ${MAX_ATTEMPTS} attempts."
    echo "Check that keycloak/ae-realm-realm.json was imported correctly."
    echo "If you have an old Docker volume, run: docker compose down -v"
    exit 1
  fi
  echo "Realm ${REALM} not ready yet (${attempt}/${MAX_ATTEMPTS})..."
  attempt=$((attempt + 1))
  sleep 2
done

echo "Configuring backend service-account permissions..."
for role in manage-users query-users view-users query-clients view-clients view-realm; do
  /opt/keycloak/bin/kcadm.sh add-roles \
    -r "$REALM" \
    --uusername "$SERVICE_ACCOUNT_USERNAME" \
    --cclientid realm-management \
    --rolename "$role" >/dev/null 2>&1 \
    && echo "Assigned realm-management role: ${role}" \
    || echo "Role ${role} already assigned or not assignable."
done

echo "Keycloak realm configured."
