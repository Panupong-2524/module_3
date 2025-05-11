#!/bin/bash

# Input Parameters
RESOURCE_GROUP="module-final-rg"                          # Azure resource group
APP_SERVICE_PLAN="asp-web-southeastasia"                  # Name of your App Service Plan
LOCATION="southeastasia"                                  # Azure region
SCALE_SETTINGS_NAME="asp-web-southeastasia-autoscale-settings-plan"  # New autoscale settings name
DEFAULT_INSTANCES=1                                       # Default instance count
MIN_INSTANCES=1                                           # Minimum instances
MAX_INSTANCES=3                                           # Maximum instances

# Step 2: Create New Autoscale Settings
echo "Creating new autoscale settings for App Service Plan: $APP_SERVICE_PLAN..."
az monitor autoscale create \
  --resource-group "$RESOURCE_GROUP" \
  --resource "$APP_SERVICE_PLAN" \
  --resource-type "Microsoft.Web/serverfarms" \
  --name "$SCALE_SETTINGS_NAME" \
  --min-count "$MIN_INSTANCES" \
  --max-count "$MAX_INSTANCES" \
  --count "$DEFAULT_INSTANCES" \
  --location "$LOCATION"

# Step 3: Add Scale-Out Rule (Increase Instances when CPU > 70%)
echo "Adding Scale-Out Rule: Increase Instances when CPU > 70%..."
az monitor autoscale rule create \
  --resource-group "$RESOURCE_GROUP" \
  --autoscale-name "$SCALE_SETTINGS_NAME" \
  --condition "Percentage CPU > 70 avg 5m" \
  --scale out 1 \
  --cooldown 300  # Cooldown of 5 minutes to avoid frequent changes

# Step 4: Add Scale-In Rule (Decrease Instances when CPU < 25%)
echo "Adding Scale-In Rule: Decrease Instances when CPU < 25%..."
az monitor autoscale rule create \
  --resource-group "$RESOURCE_GROUP" \
  --autoscale-name "$SCALE_SETTINGS_NAME" \
  --condition "Percentage CPU < 25 avg 5m" \
  --scale in 1 \
  --cooldown 300  # Cooldown of 5 minutes to avoid frequent changes

# Step 5: Validate Autoscale Settings
echo "Validating new autoscale settings..."
az monitor autoscale show \
  --resource-group "$RESOURCE_GROUP" \
  --name "$SCALE_SETTINGS_NAME"

echo "Autoscale settings reset and new configuration applied successfully!"