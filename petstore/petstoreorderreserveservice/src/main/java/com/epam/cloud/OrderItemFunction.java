package com.epam.cloud;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;

import java.io.ByteArrayInputStream;
import java.util.Map;

/**
 * Hello World Function triggered via HTTP Request
 */
public class OrderItemFunction {

    private static final String CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final String CONTAINER_NAME = "order-message";
    private static final boolean IS_OVER_WRITE = true;

    @FunctionName("ServiceBusQueueTrigger")
    public void trigger(
            @ServiceBusQueueTrigger( name = "message", queueName = "order-item-queue",  connection = "ServiceBusConnection" )
            String data, final ExecutionContext context) throws Exception{
        // Log the received message
        context.getLogger().info("Received Service Bus queue message: " + data);

        // Add your business processing logic here (e.g., JSON parsing, database update)
        context.getLogger().info("Processing message: " + data);

        try {
            String fileName = getSessionId(data, context) + ".json";
            context.getLogger().info("Upload FileName : " + fileName);
            context.getLogger().info("Data :" + data);
            // Step 1: Create Blob Service Client -> Step 2: Get Container Client
            BlobContainerClient containerClient = new BlobServiceClientBuilder()
                    .endpoint(CONNECTION_STRING) // Pass the SAS token URL here
                    .buildClient()
                    .getBlobContainerClient(CONTAINER_NAME);

            // Step 3: Get Blob Client
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            // Step 4: Upload File Stream to Azure Blob Storage
            blobClient.upload(new ByteArrayInputStream(data.getBytes()), data.getBytes().length, IS_OVER_WRITE);
            context.getLogger().info("Upload data to " + CONTAINER_NAME + " successfully.");

        } catch (Exception e) {
            context.getLogger().severe("File upload failed. Error: " + e.getMessage());
            context.getLogger().info("Upload data to " + CONTAINER_NAME + " failed.");
            throw e;
        }
    }

    private static String getSessionId(String message, ExecutionContext context) throws JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map map = objectMapper.readValue(message, Map.class);
            String id = (String) map.get("id");
            if (context != null) {
                context.getLogger().info("Extracted ID: " + id);
            }
            return id;
        } catch (Exception e) {
            context.getLogger().info("Failed to parse JSON: " + e.getMessage());
            throw e;
        }
    }

}