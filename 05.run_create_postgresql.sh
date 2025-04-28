#!/bin/bash

# Parameters
RESOURCE_GROUP=$1
REGION=$2
POSTGRES_NAME=$3
DB_USERNAME=$4
DB_PASSWORD=$5

# Validate input
if [ "$#" -ne 5 ]; then
    echo "Usage: $0 <resource-group> <region> <postgres-name> <db-username> <db-password>"
    exit 1
fi

# Create resource group
az group create --name $RESOURCE_GROUP --location $REGION

# Create PostgreSQL Flexible Server
az postgres flexible-server create \
    --resource-group $RESOURCE_GROUP \
    --name $POSTGRES_NAME \
    --location $REGION \
    --admin-user $DB_USERNAME \
    --admin-password $DB_PASSWORD \
    --sku-name standard_d64ds_v4 \
    --storage-size 32 \
    --public-access 0.0.0.0

POSTGRES_URL="postgresql://${DB_USERNAME}:${DB_PASSWORD}@${POSTGRES_NAME}.postgres.database.azure.com"

# Output
echo "PostgreSQL created successfully."
echo "PostgreSQL URL: $POSTGRES_URL"
echo "Username: $DB_USERNAME"
echo "Password: $DB_PASSWORD"