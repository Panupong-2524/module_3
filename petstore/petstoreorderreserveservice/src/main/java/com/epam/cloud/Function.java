package com.epam.cloud;

import com.azure.core.implementation.util.InputStreamContent;
import com.microsoft.azure.functions.*;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobClient;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

public class Function {

    private static final String CONNECTION_STRING = System.getenv("AZURE_STORAGE_CONNECTION_STRING");
    private static final String CONTAINER_NAME = "petstore";

    @FunctionName("uploadFileToAzureBlob")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Processing file upload request");
        try {
            // Extract file from the request body (as stream)
            String data = request.getBody().orElseThrow();
            String fileName = request.getHeaders().getOrDefault("session-name", "default") + ".json";
            context.getLogger().info("Upload FileName : " + fileName);
            context.getLogger().info("Data :" + data);
            // Step 1: Create Blob Service Client
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(CONNECTION_STRING).buildClient();
            // Step 2: Get Container Client
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME); // Replace 'mycontainer' with your container name
            // Step 3: Get Blob Client
            BlobClient blobClient = containerClient.getBlobClient(fileName);
            // Step 4: Upload File Stream to Azure Blob Storage
            blobClient.upload(new ByteArrayInputStream(data.getBytes()), data.getBytes().length, true);
            // Response to Client
            return request.createResponseBuilder(HttpStatus.OK)
                    .body("File successfully uploaded to blob storage! Blob name: " + fileName)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("File upload failed. Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed")
                    .build();
        }
    }
}