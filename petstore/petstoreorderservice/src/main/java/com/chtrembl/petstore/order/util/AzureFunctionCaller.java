package com.chtrembl.petstore.order.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class AzureFunctionCaller {

    private static final String FUNCTION_URL = System.getenv("FUNCTION_URL");
    private static final String FUNCTION_KEY = System.getenv("FUNCTION_KEY");

    public static String callAzureFunctionWithRetry(String id, String payload) throws IOException {
        // Create HTTP client
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            // Create HTTP POST request
            HttpPost httpPost = new HttpPost(FUNCTION_URL);
            // Add Optional API Key (if set)
            if (FUNCTION_KEY != null && !FUNCTION_KEY.isEmpty()) {
                httpPost.addHeader("x-functions-key", FUNCTION_KEY);
            }
            // Add payload as JSON string
            StringEntity entity = new StringEntity(payload);
            httpPost.setEntity(entity);
            httpPost.addHeader("Content-Type", "application/json");
            httpPost.addHeader("session-name", id);
            // Execute the request
            CloseableHttpResponse response = httpClient.execute(httpPost);
            try {
                // Get HTTP status code
                int statusCode = response.getStatusLine().getStatusCode();
                // Parse Response Entity
                HttpEntity responseEntity = response.getEntity();
                String responseString = responseEntity != null ? EntityUtils.toString(responseEntity) : "";
                // Handle HTTP status codes
                if (statusCode >= 200 && statusCode < 300) {
                    return responseString;
                } else {
                    throw new Exception("Azure Function Request failed with status code "
                            + statusCode + ". Response: " + responseString);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                response.close();
            }
        } finally {
            httpClient.close(); // Close the HTTP client to free resources
        }
    }

}