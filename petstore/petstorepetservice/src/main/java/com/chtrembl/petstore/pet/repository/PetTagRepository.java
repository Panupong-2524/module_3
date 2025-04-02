package com.chtrembl.petstore.pet.repository;

import com.chtrembl.petstore.pet.entity.PetTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetTagRepository extends JpaRepository<PetTag, Long> {
}