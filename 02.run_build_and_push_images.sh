#!/bin/bash
# bash run.sh demo-rg eastus demopetstorecr C:\epam_cloudx\03.app_service\00.app\cloudx-java-azure-dev\petstore

if [ $# -ne 4 ]; then
    echo "Usage: $0 <name> <location> <acr> <base_dir>"
    exit 1
fi

echo "Build Image and Push to ACR"

RG_NAME=$1
RG_LOCATION=$2
ACR_NAME=$3
BASE_DIR=$4

PET_STORE_APP="petstoreapp"
PET_STORE_PET_SERVICE="petstorepetservice"
PET_STORE_ORDER_SERVICE="petstoreorderservice"
PET_STORE_PRODUCT_SERVICE="petstoreproductservice"

run_command_docker_build() {
local folder_name=$1
local COMMAND="docker build -t $folder_name ."
run_command "$COMMAND"
}

run_command_push_image_to_acr() {
local acr_name=$1
local image_name=$2
local COMMAND="docker push $acr_name.azurecr.io/$image_name:latest"
run_command "$COMMAND"
}

run_command() {
    # Assign given command
    local COMMAND=$1

    # Execute the command
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

cd "$BASE_DIR"

run_command_docker_build $PET_STORE_PRODUCT_SERVICE
run_command_docker_build $PET_STORE_ORDER_SERVICE
run_command_docker_build $PET_STORE_PET_SERVICE
run_command_docker_build $PET_STORE_APP

az acr login --name $ACR_NAME .

run_command_push_image_to_acr $ACR_NAME $PET_STORE_PRODUCT_SERVICE
run_command_push_image_to_acr $ACR_NAME $PET_STORE_ORDER_SERVICE
run_command_push_image_to_acr $ACR_NAME $PET_STORE_PET_SERVICE
run_command_push_image_to_acr $ACR_NAME $PET_STORE_APP

echo "Build Image and Push to ACR successfully"