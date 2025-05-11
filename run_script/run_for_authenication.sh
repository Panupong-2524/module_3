#!/bin/bash

echo "Build Image, Tag Locally, and Push to ACR"

MODULE_NAME=module-final
RG_NAME=module-final-rg
RG_LOCATION=southeastasia
REGION=southeastasia
ACR_NAME=petshopboyzacr
BASE_DIR=/c/Users/Lenovo/IdeaProjects/module_3/petstore
IMAGE_VERSION=latest

#echo "Start Assign require for Authentication..."
#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-web-app-southeastasia TENANT_ID=module10epamtenant BASE_URI=https://module10epamtenant.b2clogin.com/
#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-web-app-southeastasia HOME_URL=https://petshopboyz-module-final-web-app-southeastasia.azurewebsites.net CLIENT_ID=518bfa7c-45f6-4432-a662-0a9f9e0b95d6
#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-web-app-southeastasia CLIENT_SECRET=LRP8Q~-Ov0bF7vT~cdTToIdfCQunKMUjNWAa-civ LOGIN_URL=https://petshopboyz-module-final-web-app-southeastasia.azurewebsites.net/login/oauth/code/petstore-app
#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-web-app-southeastasia USER_FLOW=B2C_1_module_final_app LOGOUT_SUCCESS_URL=https://petshopboyz-module-final-web-app-southeastasia.azurewebsites.net
#echo "End Assign require for Authentication..."

PET_STORE_APP="petstoreapp"
#PET_STORE_PET_SERVICE="petstorepetservice"
#PET_STORE_ORDER_SERVICE="petstoreorderservice"
#PET_STORE_ORDER_RESERVER_SERVICE="petstoreorderreserveservice"
#PET_STORE_PRODUCT_SERVICE="petstoreproductservice"


run_command_docker_build() {
    local folder_name=$1
    cd $folder_name
    local COMMAND="docker build -t $folder_name ."
    run_command "$COMMAND"
    cd ..
}

run_command_tag_image_locally() {
    local acr_name=$1
    local image_name=$2
    local tagged_image="$acr_name.azurecr.io/$image_name:$IMAGE_VERSION"
    local COMMAND="docker tag $image_name $tagged_image"
    run_command "$COMMAND"
}

run_command_push_image_to_acr() {
    local acr_name=$1
    local image_name=$2
    local COMMAND="docker push $acr_name.azurecr.io/$image_name:$IMAGE_VERSION"
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

# Build Docker Images
#run_command_docker_build $PET_STORE_PRODUCT_SERVICE
#run_command_docker_build $PET_STORE_ORDER_SERVICE
#run_command_docker_build $PET_STORE_PET_SERVICE
run_command_docker_build $PET_STORE_APP

# Login to Azure Container Registry
az acr login --name $ACR_NAME

# Tag Docker Images Locally
#run_command_tag_image_locally $ACR_NAME $PET_STORE_PRODUCT_SERVICE
#run_command_tag_image_locally $ACR_NAME $PET_STORE_ORDER_SERVICE
#run_command_tag_image_locally $ACR_NAME $PET_STORE_PET_SERVICE
run_command_tag_image_locally $ACR_NAME $PET_STORE_APP

# Push Docker Images to Azure Container Registry
#run_command_push_image_to_acr $ACR_NAME $PET_STORE_PRODUCT_SERVICE
#run_command_push_image_to_acr $ACR_NAME $PET_STORE_ORDER_SERVICE
#run_command_push_image_to_acr $ACR_NAME $PET_STORE_PET_SERVICE
run_command_push_image_to_acr $ACR_NAME $PET_STORE_APP

echo "Build, Tag Locally, and Push to ACR successfully"

az webapp restart --name petshopboyz-module-final-web-app-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-product-service-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-pet-service-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-order-service-southeastasia  --resource-group module-final-rg

echo "End Restart Application"
