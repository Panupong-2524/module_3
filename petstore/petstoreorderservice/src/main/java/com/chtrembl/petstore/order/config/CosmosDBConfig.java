package com.chtrembl.petstore.order.config;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCosmosRepositories
public class CosmosDBConfig extends AbstractCosmosConfiguration {

    @Value("${azure.cloud.cosmos.endpoint:}")
    private String endpoint;

    @Value("${azure.cloud.cosmos.key:}")
    private String key;

    @Value("${azure.cloud.cosmos.database:}")
    private String database;

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        return new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key);
    }

    @Override
    public String getDatabaseName() {
        return database;
    }
}