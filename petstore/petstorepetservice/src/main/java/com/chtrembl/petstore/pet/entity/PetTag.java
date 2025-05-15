package com.chtrembl.petstore.pet.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "pet_tag", schema = "public")
public class PetTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false, referencedColumnName = "id")
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false, referencedColumnName = "id")
    private Tag tag;

}