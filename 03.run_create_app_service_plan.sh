#!/bin/bash

# Check for minimum required parameters: script needs at least 5 arguments
if [ $# -lt 1 ]; then
    echo "Usage: $0 <resource_group>"
    exit 1
fi

RESOURCE_GROUP=$1
# Using Linux OS
OS_TYPE="--is-linux"
# Standard S1
SKU="S1" 

# Function to create an App Service Plan
create_app_service_plan() {
    local plan_name=$1
    local region=$2

    echo "Creating App Service Plan '$plan_name' in '$region'..."

    local COMMAND="az appservice plan create \
        --name $plan_name \
        --resource-group $RESOURCE_GROUP \
        $OS_TYPE \
        --location $region \
        --sku $SKU"

    echo "Executing: $COMMAND"

    $COMMAND

    # Check if the command was successful
    if [ $? -eq 0 ]; then
        echo "Successfully created App Service Plan: $plan_name"
    else
        echo "Failed to create App Service Plan: $plan_name"
        exit 1
    fi
}


# Creating the App Service Plans with specified parameters
create_app_service_plan "asp-api-eastus" "eastus"
create_app_service_plan "asp-web-eastus" "eastus"
create_app_service_plan "asp-web-westeurope" "westeurope"

echo "All specified App Service Plans have been created successfully."
