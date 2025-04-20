#!/bin/bash

# Parameters
RESOURCE_GROUP=$1
REGION=$2
VAULT_NAME=$3
POSTGRES_URL=$4
DB_USERNAME=$5
DB_PASSWORD=$6

# Validate input
if [ "$#" -ne 6 ]; then
    echo "Usage: $0 <resource-group> <region> <vault-name> <postgres-url> <db-username> <db-password>"
    exit 1
fi

# Create resource group if needed
az group create --name $RESOURCE_GROUP --location $REGION

# Create Key Vault
az keyvault create --name $VAULT_NAME --resource-group $RESOURCE_GROUP --location $REGION

# Add secrets to Key Vault
az keyvault secret set --name "DB-URL" --vault-name $VAULT_NAME --value $POSTGRES_URL
az keyvault secret set --name "DB-USERNAME" --vault-name $VAULT_NAME --value $DB_USERNAME
az keyvault secret set --name "DB-PASSWORD" --vault-name $VAULT_NAME --value $DB_PASSWORD

# Get secret URIs
DB_URL_URI=$(az keyvault secret show --name "DB-URL" --vault-name $VAULT_NAME --query "id" -o tsv)
DB_USERNAME_URI=$(az keyvault secret show --name "DB-USERNAME" --vault-name $VAULT_NAME --query "id" -o tsv)
DB_PASSWORD_URI=$(az keyvault secret show --name "DB-PASSWORD" --vault-name $VAULT_NAME --query "id" -o tsv)

# Output
echo "Key Vault created successfully."
echo "DB_URL_URI: $DB_URL_URI"
echo "DB_USERNAME_URI: $DB_USERNAME_URI"
echo "DB_PASSWORD_URI: $DB_PASSWORD_URI"