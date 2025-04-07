package com.chtrembl.petstore.order.entity;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.chtrembl.petstore.order.model.Product;
import com.chtrembl.petstore.order.model.Tag;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.threeten.bp.OffsetDateTime;

import java.util.List;

@Data
@Container(containerName = "OrderContainer", autoCreateContainer = false)
public class Order {

    @PartitionKey
    private String orderId;

    private String id = null;

    private String email = null;

    private List<Product> products = null;

    private OffsetDateTime shipDate = null;

    private List<Tag> tags = null;

    private String status = null;

    private boolean complete;

    public Order() {

    }

    public Order(com.chtrembl.petstore.order.model.Order o) {
        this.id = o.getId();
        this.email = o.getEmail();
        this.products = o.getProducts();
        this.tags = o.getTags();
        this.status = o.getStatus().toString();
        this.shipDate = o.getShipDate();
        this.complete = o.isComplete();
    }

}
