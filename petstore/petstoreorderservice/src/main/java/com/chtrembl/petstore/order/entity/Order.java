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

    @JsonProperty("products")
    private List<Product> products = null;

    @JsonProperty("shipDate")
    private OffsetDateTime shipDate = null;

    @JsonProperty("tags")
    private List<Tag> tags = null;

    private String status = null;

}
