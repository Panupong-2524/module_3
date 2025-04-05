package com.chtrembl.petstore.product.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


@Data
@Entity
@Table(name = "product_tag", schema = "public")
public class ProductTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false, referencedColumnName = "id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false, referencedColumnName = "id")
    private Tag tag;

    // Constructors
    public ProductTag() {}

    public ProductTag(Product product, Tag tag) {
        this.product = product;
        this.tag = tag;
    }

}