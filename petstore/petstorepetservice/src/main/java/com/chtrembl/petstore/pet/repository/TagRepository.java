package com.chtrembl.petstore.pet.repository;

import com.chtrembl.petstore.pet.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findAllByName(List<String> names);
}