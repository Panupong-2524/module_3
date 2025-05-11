#!/bin/bash

# Parameters
RESOURCE_GROUP=$1
REGION=$2
COSMOS_NAME=$3
INITIAL_DB_NAME=$4
INITIAL_CONTAINER_NAME=$5

# Validate input
if [ "$#" -ne 5 ]; then
    echo "Usage: $0 <resource-group> <region> <cosmos-name> <initial-database-name> <initial-container-name>"
    exit 1
fi

# Create resource group
az group create --name $RESOURCE_GROUP --location $REGION

# Create Cosmos DB account
az cosmosdb create \
    --name $COSMOS_NAME \
    --resource-group $RESOURCE_GROUP \
    --kind MongoDB \
    --locations regionName=$REGION failoverPriority=0 \
    --default-consistency-level Eventual \
    --sku-name "Standard"

# Create initial database
az cosmosdb mongodb database create \
    --account-name $COSMOS_NAME \
    --resource-group $RESOURCE_GROUP \
    --name $INITIAL_DB_NAME

# Create initial container
az cosmosdb mongodb collection create \
    --account-name $COSMOS_NAME \
    --resource-group $RESOURCE_GROUP \
    --database-name $INITIAL_DB_NAME \
    --name $INITIAL_CONTAINER_NAME

# Get primary access key
PRIMARY_KEY=$(az cosmosdb keys list --name $COSMOS_NAME --resource-group $RESOURCE_GROUP --type keys --query "primaryMasterKey" -o tsv)

# Output
echo "Cosmos DB created successfully."
echo "Cosmos DB URI: https://${COSMOS_NAME}.mongo.cosmos.azure.com"
echo "Primary Key: $PRIMARY_KEY"