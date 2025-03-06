#!/bin/bash
# demo-rg
# eastus

# Check for the right number of input arguments
if [ $# -ne 3 ]; then
    echo "Usage: $0 <name> <location> <acr>"
    exit 1
fi

RG_NAME=$1
RG_LOCATION=$2
ACR_NAME=$3

# Command to create a resource group
COMMAND="az group create --name $RG_NAME --location $RG_LOCATION"

# Execute the command
echo "Executing: $COMMAND"
$COMMAND

# Check the exit status of the last command executed
if [ $? -eq 0 ]; then
    echo "Successfully created the resource group."
else
    echo "Failed to create the resource group."
    exit 1
fi

# Define the SKU and admin-enabled options
SKU="Basic"
ADMIN_ENABLED="true"

# Command to create Azure Container Registry
COMMAND_ACR="az acr create \
    --resource-group $RG_NAME \
    --name $ACR_NAME \
    --sku $SKU \
    --admin-enabled $ADMIN_ENABLED"

echo "Executing: $COMMAND_ACR"
$COMMAND_ACR

# Check if the command was successful
if [ $? -eq 0 ]; then
    echo "Successfully created the Azure Container Registry"
else
    echo "Failed to create the Azure Container Registry"
    exit 1
fi
