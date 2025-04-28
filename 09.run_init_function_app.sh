#!/bin/bash

# Validate input
if [ "$#" -ne 4 ]; then
    echo "Usage: $0 <resource-group> <region> <storage-account-name> <app-service-plan> <function-name>"
    exit 1
fi

RESOURCE_GROUP=$1
REGION=$2
STORAGE_ACCOUNT=$3
APP_SERVICE_PLAN=$4
FUNCTION_APP_NAME=$4

run_command() {

  local COMMAND=$1

  echo "Executing: $COMMAND"

  $COMMAND

  # Check the exit status of the last command executed
  if [ $? -eq 0 ]; then
      echo "Successfully run command [$COMMAND]"
  else
      echo "Failed to run command [$COMMAND]"
      exit 1
  fi

}

STORAGE_ACCOUNT_COMMAND='az storage account create --name $STORAGE_ACCOUNT --resource-group $RESOURCE_GROUP --location $REGION --sku Standard_LRS --kind StorageV2'
APP_SERVICE_PLAN_COMMAND='az appservice plan create --name $APP_SERVICE_PLAN --resource-group $RESOURCE_GROUP --location $REGION --sku S1 --is-linux '
FUNCTION_APP_COMMAND='az functionapp create --name my-function-app --resource-group module-9-rg --storage-account module9storage --plan asp-func --runtime java --runtime-version 17'

run_command $STORAGE_ACCOUNT_COMMAND
run_command $APP_SERVICE_PLAN_COMMAND
run_command $FUNCTION_APP_COMMAND

echo "Deployment to Chain Account storage + App service plan + Function app successfully."
