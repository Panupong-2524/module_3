package com.chtrembl.petstore.product.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Set;

@Data
@Entity
@Table(name = "product", schema = "public") // Matches PostgreSQL name and schema
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64) // VARCHAR(64) NOT NULL UNIQUE
    private String name;

    @ManyToOne // Foreign key reference to Category
    @JoinColumn(name = "category_id", nullable = false, referencedColumnName = "id") // Matches FK constraint
    private Category category;

    @Column(nullable = false, length = 255) // PhotoURL VARCHAR(255) NOT NULL
    private String photoURL;

    @Column(nullable = false, length = 64) // Status VARCHAR(64) NOT NULL
    private String status;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "pet_tag",
            schema = "public",
            joinColumns = @JoinColumn(name = "pet_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id", referencedColumnName = "id")
    )
    private Set<Tag> tags;

}