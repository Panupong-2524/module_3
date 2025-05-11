#!/bin/bash



MODULE_NAME=module-final
RG_NAME=$MODULE_NAME-rg
REGION=southeastasia
ACR_NAME=petshopboyzacr
VERSION=latest

PET_SERVICE_URL=https://petshopboyz-$MODULE_NAME-pet-service-southeastasia.azurewebsites.net
PRODUCT_SERVICE_URL=https://petshopboyz-$MODULE_NAME-product-service-southeastasia.azurewebsites.net
ORDER_SERVICE_URL=https://petshopboyz-$MODULE_NAME-order-service-southeastasia.azurewebsites.net

# bash ./01.run_create_resource_and_acr.sh $RG_NAME $REGION $ACR_NAME
#
bash ./02.run_build_and_push_images.sh $RG_NAME $REGION $ACR_NAME /c/Users/Lenovo/IdeaProjects/module_3/petstore $VERSION

# bash ./03.run_create_app_service_plan.sh $RG_NAME $REGION

# bash ./04.00.run_deploy_image_to_app_service.sh $MODULE_NAME $RG_NAME $REGION $ACR_NAME

az webapp restart --name petshopboyz-module-final-web-app-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-product-service-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-pet-service-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-order-service-southeastasia  --resource-group module-final-rg


#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-pet-service-southeastasia WEBSITES_PORT=8080
#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-product-service-southeastasia WEBSITES_PORT=8080
#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-order-service-southeastasia WEBSITES_PORT=8080 PETSTOREPRODUCTSERVICE_URL=$PRODUCT_SERVICE_URL
#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-web-app-southeastasia WEBSITES_PORT=8080 PETSTOREPETSERVICE_URL=$PET_SERVICE_URL
#sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-web-app-southeastasia PETSTOREPRODUCTSERVICE_URL=$PRODUCT_SERVICE_URL PETSTOREORDERSERVICE_URL=ORDER_SERVICE_URL
#

# sh ./04.01.run_add_env_parameter.sh $RG_NAME $REGION petshopboyz-$MODULE_NAME-web-app-southeastasia TENANT_ID=module10epamtenant BASE_URI=https://module10epamtenant.b2clogin.com/ HOME_URL=https://petshopboyz-module-9-pet-service-southeastasia.azurewebsites.net CLIENT_ID=e5b49661-1e3e-4205-8268-2c4c79e23039 CLIENT_SECRET=avy8Q~~r~qQZr7Y012NyDF2p3eOYWc-on5DtXa77 LOGIN_URL=https://petshopboyz-module-9-web-app-southeastasia.azurewebsites.net/login/oauth2/code/petstore-app USER_FLOW=B2C_1_signup_and_signin_v2 LOGOUT_SUCCESS_URL=https://petshopboyz-module-9-web-app-southeastasia.azurewebsites.net
# bash ./99.run_create_postgresql.sh module-8-rg southeastasia module8postgresql idcadmin Islamic@12345



