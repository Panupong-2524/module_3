package com.chtrembl.petstore.pet.service;

import com.chtrembl.petstore.pet.entity.Pet;
import com.chtrembl.petstore.pet.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepository;

    @Transactional
    public List<Pet> findPetByStatuses(List<String> status) {
        return petRepository.findByStatusIn(status);
    }

}