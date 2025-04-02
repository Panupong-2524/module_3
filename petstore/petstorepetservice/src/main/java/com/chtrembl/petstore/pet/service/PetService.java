package com.chtrembl.petstore.pet.service;

import com.chtrembl.petstore.pet.entity.Pet;
import com.chtrembl.petstore.pet.repository.PetRepository;
import com.chtrembl.petstore.pet.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private TagRepository tagRepository;

    @Transactional
    public List<Pet> findPetByStatuses(List<String> status) {
        return petRepository.findByStatusIn(status);
    }

//    getPetById
//    addPet
//    deletePet
//    updatePet
//    findPetsByTags

}
