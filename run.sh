#!/bin/bash

sh ./04.01.run_add_env_parameter.sh module-10-rg southeastasia petshopboyz-module-9-web-app-southeastasia TENANT_ID=module10epamtenant BASE_URI=https://module10epamtenant.b2clogin.com/ HOME_URL=https://petshopboyz-module-9-pet-service-southeastasia.azurewebsites.net CLIENT_ID=e5b49661-1e3e-4205-8268-2c4c79e23039 CLIENT_SECRET=avy8Q~~r~qQZr7Y012NyDF2p3eOYWc-on5DtXa77 LOGIN_URL=https://petshopboyz-module-9-web-app-southeastasia.azurewebsites.net/login/oauth2/code/petstore-app USER_FLOW=B2C_1_signup_and_signin_v2 LOGOUT_SUCCESS_URL=https://petshopboyz-module-9-web-app-southeastasia.azurewebsites.net

# bash ./01.run_create_resource_and_acr.sh module-8-rg southeastasia petshopboyzcr
#
bash ./02.run_build_and_push_images.sh module-10-rg southeastasia petshopboyzacr /c/Users/Lenovo/IdeaProjects/module_3/petstore latest
#
# bash ./03.run_create_app_service_plan.sh module-10-rg southeastasia
#
bash ./04.00.run_deploy_image_to_app_service.sh module-10-rg southeastasia petshopboyzacr


# sh ./04.01.run_add_env_parameter.sh module-10-rg southeastasia petshopboyz-module-9-pet-service-southeastasia WEBSITES_PORT=8080
# sh ./04.01.run_add_env_parameter.sh module-10-rg southeastasia petshopboyz-module-9-product-service-southeastasia WEBSITES_PORT=8080
# sh ./04.01.run_add_env_parameter.sh module-10-rg southeastasia petshopboyz-module-9-order-service-southeastasia WEBSITES_PORT=8080 PETSTOREPRODUCTSERVICE_URL=https://petshopboyz-module-9-product-service-southeastasia.azurewebsites.net
# sh ./04.01.run_add_env_parameter.sh module-10-rg southeastasia petshopboyz-module-9-web-app-southeastasia WEBSITES_PORT=8080 PETSTOREPETSERVICE_URL=https://petshopboyz-module-9-pet-service-southeastasia.azurewebsites.net PETSTOREPRODUCTSERVICE_URL=https://petshopboyz-module-9-product-service-southeastasia.azurewebsites.net PETSTOREORDERSERVICE_URL=https://petshopboyz-module-9-order-service-southeastasia.azurewebsites.net

#bash ./05.run_create_postgresql.sh module-8-rg southeastasia module8postgresql idcadmin Islamic@12345



