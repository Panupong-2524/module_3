#!/bin/bash

if [ $# -lt 2 ]; then
    echo "Usage: $0 <resource-group> <region> <acr-name>"
    exit 1
fi

RG_NAME=$1
REGION=$2
ACR_NAME=$3

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
deploy_app "petshopboyz-module-9-web-app-$REGION" "asp-web-$REGION" "petstoreapp" "$REGION"

# Deploy to API Plan
#deploy_app "petshopboyz-module-9-pet-service-$REGION" "asp-api-$REGION" "petstorepetservice" "$REGION"
#deploy_app "petshopboyz-module-9-order-service-$REGION" "asp-api-$REGION" "petstoreorderservice" "$REGION"
#deploy_app "petshopboyz-module-9-product-service-$REGION" "asp-api-$REGION" "petstoreproductservice" "$REGION"

echo "Deployment to APP service successfully"
