#!/usr/bin/env bash
set -euxo pipefail

: "${IMAGE_TAG:?IMAGE_TAG env is required}"
: "${IMAGE_REPO:?IMAGE_REPO env is required}"

mkdir -p ~/oter
cd ~/oter

echo "Working dir: $(pwd)"
ls -la || true

# Ensure Docker Compose is available
if docker compose version >/dev/null 2>&1; then
  COMPOSE="docker compose"
elif docker-compose version >/dev/null 2>&1; then
  COMPOSE="docker-compose"
else
  if command -v apt-get >/dev/null 2>&1; then
    sudo apt-get update -y && sudo apt-get install -y docker-compose-plugin
    COMPOSE="docker compose"
  elif command -v dnf >/dev/null 2>&1 || command -v yum >/dev/null 2>&1; then
    (command -v dnf >/dev/null 2>&1 && sudo dnf install -y docker-compose-plugin) || sudo yum install -y docker-compose-plugin
    COMPOSE="docker compose"
  else
    echo "Docker Compose not found and cannot auto-install" >&2
    exit 1
  fi
fi

# Optional: if you had an old standalone container called nginx-proxy, remove it to avoid name conflicts
docker rm -f nginx-proxy || true

# GHCR login if creds provided
if [[ -n "${GHCR_READ_USER:-}" && -n "${GHCR_READ_TOKEN:-}" ]]; then
  echo "${GHCR_READ_TOKEN}" | docker login ghcr.io -u "${GHCR_READ_USER}" --password-stdin
fi

mkdir -p certbot/conf certbot/www
echo "Deploying ${IMAGE_REPO}:${IMAGE_TAG}"

export IMAGE_REPO IMAGE_TAG

# Validate compose (expands envs)
${COMPOSE} -p oter -f docker-compose.yml config >/dev/null

# Ensure network exists
docker network create edge || true

# Pull & start
${COMPOSE} -p oter -f docker-compose.yml pull
${COMPOSE} -p oter -f docker-compose.yml up -d --remove-orphans

# ==== Auto diagnostics ====
echo "=== Service status ==="
${COMPOSE} -p oter ps || true

# If oter exists, check health/state and recent logs
OTER_CID="$(docker ps -aq -f name=oter_oter)"
if [[ -n "${OTER_CID}" ]]; then
  echo "=== oter inspect (State) ==="
  docker inspect --format='{{json .State}}' "${OTER_CID}" || true
  echo "=== oter logs (last 200 lines) ==="
  docker logs --tail=200 "${OTER_CID}" || true
fi

# Also show nginx logs if present
NGINX_CID="$(docker ps -aq -f name=oter_nginx)"
if [[ -n "${NGINX_CID}" ]]; then
  echo "=== nginx logs (last 100 lines) ==="
  docker logs --tail=100 "${NGINX_CID}" || true
fi
