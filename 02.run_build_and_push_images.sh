#!/bin/bash
# bash run.sh demo-rg eastus module4acr /c/azure_cloudx/04.container_app/00.app/cloudx-java-azure-dev/petstore
# sh 02.run_build_and_push_images.sh module-4-demo-rg eastus module4acr /c/azure_cloudx/04.container_app/00.app/cloudx-java-azure-dev/petstore v1
# sh 02.run_build_and_push_images.sh module-4-demo-rg eastus module4acr /c/azure_cloudx/04.container_app/00.app/cloudx-java-azure-dev/petstore v2
# 
if [ $# -ne 5 ]; then
    echo "Usage: $0 <name> <location> <acr> <base_dir> <image_version>"
    exit 1
fi

echo "Build Image, Tag Locally, and Push to ACR"

RG_NAME=$1
RG_LOCATION=$2
ACR_NAME=$3
BASE_DIR=$4
IMAGE_VERSION=$5

PET_STORE_APP="petstoreapp"
PET_STORE_PET_SERVICE="petstorepetservice"
PET_STORE_ORDER_SERVICE="petstoreorderservice"
PET_STORE_ORDER_RESERVER_SERVICE="petstoreorderreserveservice"
PET_STORE_PRODUCT_SERVICE="petstoreproductservice"


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