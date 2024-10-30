package com.example.PushOfLife.service;

import com.example.PushOfLife.dto.aed.AedRequestDTO;
import com.example.PushOfLife.dto.aed.AedDetailsResponseDTO;
import com.example.PushOfLife.dto.aed.AedResponseDTO;
import com.example.PushOfLife.entity.AedAvailableEntity;
import com.example.PushOfLife.entity.AedEntity;
import com.example.PushOfLife.repository.AedAvailableRepository;
import com.example.PushOfLife.repository.AedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AedLocationService {

    private final AedRepository aedRepository;
    private final AedAvailableRepository aedAvailableRepository;

    public List<AedResponseDTO> getAedLocation(Double norLatitude, Double norLongitude, Double southLatitude, Double southLongitude) {
        List<AedEntity> findAed = aedRepository.findByAedLatitudeBetweenAndAedLongitudeBetween(southLatitude, norLatitude, southLongitude, norLongitude);
        List<AedResponseDTO> aedList = new ArrayList<>();
        for (AedEntity aedEntity : findAed) {
            AedResponseDTO aedResponseDTO = new AedResponseDTO();
            aedResponseDTO.fromEntity(aedEntity);
            aedList.add(aedResponseDTO);
        }
        return aedList;
    }

    public AedDetailsResponseDTO getAedDetails(Integer aedId){
        AedEntity findAed = aedRepository.findById(aedId).orElseThrow(() -> new RuntimeException("Aed Not Found"));
        AedAvailableEntity findAedAvailable = aedAvailableRepository.findById(aedId).orElseThrow(() -> new RuntimeException("AedAvailable Not Found"));
        AedDetailsResponseDTO aedResponseDTO = new AedDetailsResponseDTO();
        aedResponseDTO.fromEntity(findAed, findAedAvailable);
        return aedResponseDTO;
    }
}
