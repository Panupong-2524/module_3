#!/bin/bash

az webapp restart --name petshopboyz-module-final-web-app-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-product-service-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-pet-service-southeastasia  --resource-group module-final-rg
az webapp restart --name petshopboyz-module-final-order-service-southeastasia  --resource-group module-final-rg
