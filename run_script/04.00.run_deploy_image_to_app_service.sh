#!/bin/bash

if [ $# -lt 2 ]; then
    echo "Usage: $0 <module-name> <resource-group> <region> <acr-name>"
    exit 1
fi

MODULE_NAME=$1
RG_NAME=$2
REGION=$3
ACR_NAME=$4

# Function to create a web app and deploy a container from ACR
deploy_app() {
  local appName=$1
  local appPlan=$2
  local image=$3
  local region=$4

  echo "Creating web app '$appName' in region '$region' using plan '$appPlan' to run container image '$image'"

  # Create App Service
    echo "Creating Azure Web App from ACR container..."
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

    echo "Enabling Continuous Deployment for Azure Web App..."
    COMMAND="az webapp deployment container config \
      --name $appName \
      --resource-group $RG_NAME \
      --enable-cd true"

    echo "Executing: $COMMAND"

    $COMMAND

    # Check if the command was successful
    if [ $? -eq 0 ]; then
        echo "Enabled Continuous Deployment successfully"
    else
        echo "Enabled Continuous Deployment failed"
        exit 1
    fi
}

# Deploy to Web Plans
deploy_app "petshopboyz-$MODULE_NAME-web-app-$REGION" "asp-web-$REGION" "petstoreapp" "$REGION"

# Deploy to API Plan
deploy_app "petshopboyz-$MODULE_NAME-pet-service-$REGION" "asp-api-$REGION" "petstorepetservice" "$REGION"
deploy_app "petshopboyz-$MODULE_NAME-order-service-$REGION" "asp-api-$REGION" "petstoreorderservice" "$REGION"
deploy_app "petshopboyz-$MODULE_NAME-product-service-$REGION" "asp-api-$REGION" "petstoreproductservice" "$REGION"

echo "Deployment to APP service successfully"

