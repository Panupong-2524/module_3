package com.chtrembl.petstore.product.service;

import com.chtrembl.petstore.product.entity.Product;
import com.chtrembl.petstore.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getProductByStatuses(List<String> status) {
        return productRepository.findByStatusIn(status);
    }

}
