package com.example.PushOfLife.service;

import com.example.PushOfLife.dto.aed.AedRequestDTO;
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

    public List<AedResponseDTO> getAedLocation(AedRequestDTO aedRequestDTO) {
        List<AedEntity> findAed = aedRepository.findByAedLatitudeBetweenAndAedLongitudeBetween(aedRequestDTO.getSouLatitude(), aedRequestDTO.getNorLatitude(), aedRequestDTO.getSouLongitude(), aedRequestDTO.getNorLongitude());
        List<AedResponseDTO> aedList = new ArrayList<>();
        for (AedEntity aedEntity : findAed) {
            AedAvailableEntity aedAvailableEntity = aedAvailableRepository.findById(aedEntity.getId()).orElseThrow(() -> new RuntimeException("Aed Not Found"));
            AedResponseDTO aedResponseDTO = new AedResponseDTO();
            aedResponseDTO.fromEntity(aedEntity, aedAvailableEntity);
            aedList.add(aedResponseDTO);
        }
        return aedList;
    }
}
