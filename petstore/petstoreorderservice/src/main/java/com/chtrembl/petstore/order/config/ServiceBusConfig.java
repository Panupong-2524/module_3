package com.chtrembl.petstore.order.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceBusConfig {

    @Value("${servicebus.connection-string}")
    private String connectionString;

    @Value("${servicebus.queue-name}")
    private String queueName;

    public String getConnectionString() {
        return connectionString;
    }

    public String getQueueName() {
        return queueName;
    }
}