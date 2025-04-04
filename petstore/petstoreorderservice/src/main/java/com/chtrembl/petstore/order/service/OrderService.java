package com.chtrembl.petstore.order.service;

import com.chtrembl.petstore.order.entity.Order;
import com.chtrembl.petstore.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    // Saves a new order in CosmosDB
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    // Fetches all orders from CosmosDB
    public Iterable<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Retrieves a single order by ID from CosmosDB
    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }
}