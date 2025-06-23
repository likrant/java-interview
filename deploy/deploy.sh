#!/bin/bash
set -e

if [ -z "$1" ]; then
  echo "Usage: $0 <env-file>" >&2
  exit 1
fi

ENV_FILE="$1"
if [ ! -f "$ENV_FILE" ]; then
  echo "Environment file '$ENV_FILE' not found" >&2
  exit 1
fi

# Load variables from the env file
set -o allexport

source "$ENV_FILE"
set +o allexport

# Ensure Azure CLI is installed
if ! command -v az >/dev/null 2>&1; then
  echo "Azure CLI (az) not found. Please install it: https://learn.microsoft.com/en-us/cli/azure/install-azure-cli" >&2
  exit 1
fi

# Login and push image to ACR
if ! az account show >/dev/null 2>&1; then
  az login --tenant "$TENANT_ID"
fi
if ! az account show --subscription "$SUBSCRIPTION_ID" >/dev/null 2>&1; then
  az login --tenant "$TENANT_ID"
fi
az account set --subscription "$SUBSCRIPTION_ID"
az acr login --name "$ACR_NAME"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
docker build -f "$SCRIPT_DIR/Dockerfile" -t "$IMAGE_NAME" "$REPO_ROOT"
docker tag "$IMAGE_NAME" "$ACR_NAME.azurecr.io/$IMAGE_NAME"
docker push "$ACR_NAME.azurecr.io/$IMAGE_NAME"

