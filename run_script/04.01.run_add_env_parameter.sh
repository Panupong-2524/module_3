#!/bin/bash

# Parameters
RESOURCE_GROUP=$1
REGION=$2
WEBAPP_NAME=$3
shift 3 # Shift the first three parameters so the remaining arguments are treated as key=value pairs

# Validate input
if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <resource-group> <region> <webapp-name> <key=value> [<key=value> ...]"
    exit 1
fi

# Prepare environment settings string
ENV_SETTINGS=""
for KEY_VALUE in "$@"; do
    KEY=$(echo "$KEY_VALUE" | cut -d "=" -f 1)
    VALUE=$(echo "$KEY_VALUE" | cut -d "=" -f 2-)

    if [ -z "$KEY" ] || [ -z "$VALUE" ]; then
        echo "Invalid key=value pair: $KEY_VALUE. Ensure correct formatting."
        exit 1
    fi

    ENV_SETTINGS="${ENV_SETTINGS}${KEY}=${VALUE} "
done

# Add the environment variables to the Azure Web App
echo "Adding environment variables ($ENV_SETTINGS) to Web App: $WEBAPP_NAME in Resource Group: $RESOURCE_GROUP..."

az webapp config appsettings set --name $WEBAPP_NAME --resource-group $RESOURCE_GROUP --settings $ENV_SETTINGS

if [ $? -eq 0 ]; then
    echo "Successfully set environment variables for Web App: $WEBAPP_NAME."
else
    echo "Failed to set environment variables for Web App: $WEBAPP_NAME."
    exit 1
fi