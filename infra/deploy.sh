#!/usr/bin/env bash
set -euxo pipefail

: "${IMAGE_TAG:?IMAGE_TAG env is required}"
: "${IMAGE_REPO:?IMAGE_REPO env is required}"

mkdir -p ~/oter
cd ~/oter

echo "Working dir: $(pwd)"
ls -la || true

# Ensure Docker Compose is available
if docker compose version >/dev/null 2>&1; then COMPOSE="docker compose";
elif docker-compose version >/dev/null 2>&1; then COMPOSE="docker-compose";
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

# GHCR login if creds provided (needed for private images)
if [[ -n "${GHCR_READ_USER:-}" && -n "${GHCR_READ_TOKEN:-}" ]]; then
  echo "${GHCR_READ_TOKEN}" | docker login ghcr.io -u "${GHCR_READ_USER}" --password-stdin
fi

# Ensure folders for nginx/certbot exist
mkdir -p certbot/conf certbot/www

echo "Deploying ${IMAGE_REPO}:${IMAGE_TAG} using ${COMPOSE}"

# Move nginx.conf next to the compose file name we use on the server
cp -f infra/nginx.conf ./nginx.conf

# Inject env vars at runtime
export IMAGE_REPO IMAGE_TAG

# Validate compose (expands envs)
${COMPOSE} -f infra/docker-compose.yml config >/dev/null

# Ensure network exists
docker network create edge || true

# Pull & start
${COMPOSE} -f infra/docker-compose.yml pull
${COMPOSE} -f infra/docker-compose.yml up -d

# Cleanup dangling images
docker image prune -f
