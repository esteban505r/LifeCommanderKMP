#!/usr/bin/env bash
set -euo pipefail

export DB_PASSWORD=$(aws secretsmanager get-secret-value --secret-id oter/db_password \
  --query SecretString --output text)

export DB_URL=$(aws ssm get-parameter --name /oter/db_url --query Parameter.Value --output text)
export DB_USER=$(aws ssm get-parameter --name /oter/db_user --query Parameter.Value --output text)

# Non-secrets
export ENVIRONMENT=production
export LOG_LEVEL=INFO
export AWS_REGION=us-east-1
export PORT=8080

# Export in a way docker compose can read
cat > .env.runtime <<EOF
ENVIRONMENT=$ENVIRONMENT
LOG_LEVEL=$LOG_LEVEL
AWS_REGION=$AWS_REGION
PORT=$PORT
DB_URL=$DB_URL
DB_USER=$DB_USER
DB_PASSWORD=$DB_PASSWORD
EOF
