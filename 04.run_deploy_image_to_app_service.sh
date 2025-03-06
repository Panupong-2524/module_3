#!/bin/bash

if [ $# -lt 2 ]; then
    echo "Usage: $0 <resource group> <acr name>"
    exit 1
fi

# Variables
# rgName="demo-rg"
# acrName="petshopboyzcr"

RG_NAME=$1
ACR_NAME=$2

# Function to create a web app and deploy a container from ACR
deploy_app() {
  local appName=$1
  local appPlan=$2
  local image=$3
  local region=$4

  echo "Creating web app '$appName' in region '$region' using plan '$appPlan' to run container image '$image'"

  # Create App Service
    local COMMAND="az webapp create \
--name $appName \
--plan $appPlan \
--resource-group $RG_NAME \
--deployment-container-image-name $ACR_NAME.azurecr.io/$image:latest"

    echo "Executing: $COMMAND"

    $COMMAND

    # Check if the command was successful
    if [ $? -eq 0 ]; then
        echo "Successfully deploy $image to App Service Plan: $appPlan"
    else
        echo "Failed to deploy $image to App Service Plan: $appPlan"
        exit 1
    fi
}

# Deploy to Web Plans
deploy_app "petshopboyz-web-app-eastus" "asp-web-eastus" "petstoreapp" "eastus"
deploy_app "petshopboyz-web-app-westeurope" "asp-web-westeurope" "petstoreapp" "westeurope"

# # Deploy to API Plan
deploy_app "petshopboyz-pet-service-eastus" "asp-api-eastus" "petstorepetservice" "eastus"
deploy_app "petshopboyz-order-service-eastus" "asp-api-eastus" "petstoreorderservice" "eastus"
deploy_app "petshopboyz-product-service-eastus" "asp-api-eastus" "petstoreproductservice" "eastus"

echo "Deployment to APP service successfully"