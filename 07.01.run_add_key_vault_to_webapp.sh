#!/bin/bash

# Parameters
RESOURCE_GROUP=$1
VAULT_NAME=$2
APP_1_NAME=$3
APP_2_NAME=$4

# Validate input
if [ "$#" -ne 4 ]; then
    echo "Usage: $0 <resource-group> <vault-name> <app-1-name> <app-2-name>"
    exit 1
fi

# Enable Managed Identity for App 1
echo "Enabling Managed Identity for App 1: $APP_1_NAME..."
APP_1_PRINCIPAL_ID=$(az webapp identity assign --resource-group $RESOURCE_GROUP --name $APP_1_NAME --query "principalId" -o tsv)

if [ -z "$APP_1_PRINCIPAL_ID" ]; then
    echo "Failed to enable Managed Identity for App 1 ($APP_1_NAME). Exiting."
    exit 1
fi

# Enable Managed Identity for App 2
echo "Enabling Managed Identity for App 2: $APP_2_NAME..."
APP_2_PRINCIPAL_ID=$(az webapp identity assign --resource-group $RESOURCE_GROUP --name $APP_2_NAME --query "principalId" -o tsv)

if [ -z "$APP_2_PRINCIPAL_ID" ]; then
    echo "Failed to enable Managed Identity for App 2 ($APP_2_NAME). Exiting."
    exit 1
fi

# Assign Key Vault access policies
echo "Assigning Key Vault access policy for App 1..."
az keyvault set-policy --name $VAULT_NAME --object-id $APP_1_PRINCIPAL_ID --secret-permissions get list

echo "Assigning Key Vault access policy for App 2..."
az keyvault set-policy --name $VAULT_NAME --object-id $APP_2_PRINCIPAL_ID --secret-permissions get list

# Output
echo "Managed Identity and Key Vault access configured successfully."
echo "App 1 Principal ID: $APP_1_PRINCIPAL_ID"
echo "App 2 Principal ID: $APP_2_PRINCIPAL_ID"
