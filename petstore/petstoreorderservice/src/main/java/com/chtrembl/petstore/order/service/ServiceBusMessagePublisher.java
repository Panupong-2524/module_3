package com.chtrembl.petstore.order.service;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.chtrembl.petstore.order.config.ServiceBusConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceBusMessagePublisher {

    private final ServiceBusSenderClient senderClient;

    @Autowired
    public ServiceBusMessagePublisher(ServiceBusConfig serviceBusConfig) {
        // Initialize the Service Bus Sender Client
        this.senderClient = new ServiceBusClientBuilder()
            .connectionString(serviceBusConfig.getConnectionString())
            .sender()
            .queueName(serviceBusConfig.getQueueName())
            .buildClient();
    }

    public void sendMessage(String messageContent) {
        try {
            // Send the message to the Azure Service Bus queue
            senderClient.sendMessage(new ServiceBusMessage(messageContent));
            System.out.println("Message sent to Service Bus queue: " + messageContent);
        } catch (Exception ex) {
            System.err.println("Failed to send message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // Shutdown or cleanup
    public void close() {
        senderClient.close();
    }
}