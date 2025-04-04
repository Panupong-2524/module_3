package com.chtrembl.petstore.order.config;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCosmosRepositories
public class CosmosDBConfig extends AbstractCosmosConfiguration {

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        DefaultAzureCredential credential = new DefaultAzureCredentialBuilder()
                .build();

        return new CosmosClientBuilder()
                .endpoint("https://module7cosmosdb.documents.azure.com:443/")
                .credential(credential);
    }

    @Override
    public String getDatabaseName() {
        return "OrderDatabase";
    }
}