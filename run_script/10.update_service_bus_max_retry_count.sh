#!/bin/bash

# Validate input
if [ "$#" -ne 4 ]; then
    echo "Usage: $0 <resource-group> <namespace> <queue> <max-delivery-count>"
    exit 1
fi

RESOURCE_GROUP=$1
NAME_SPACE=$2
QUEUE=$3
MAX_DELIVERY_COUNT=$4

az servicebus queue update \
--resource-group $RESOURCE_GROUP \
--namespace-name $NAME_SPACE \
--name $QUEUE \
--max-delivery-count $MAX_DELIVERY_COUNT \
--lock-duration PT1M \
--enable-dead-lettering-on-message-expiration true

#az servicebus queue update \
#    --name order-item-queue \
#    --namespace-name module9servicebus \
#    --resource-group module-9-rg \
#    --max-delivery-count 3 \
#    --lock-duration PT1M \
#    --enable-dead-lettering-on-message-expiration true