package com.example.PushOfLife.controller;

import com.example.PushOfLife.dto.aed.AedRequestDTO;
import com.example.PushOfLife.dto.aed.AedResponseDTO;
import com.example.PushOfLife.entity.UserEntity;
import com.example.PushOfLife.service.AedLocationService;
import com.example.PushOfLife.service.AedService;
import com.example.PushOfLife.service.CsvToDataBaseService;
import com.example.PushOfLife.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/POL/aed")
@RequiredArgsConstructor
public class AedController {

    private final AedService aedService;
    private final CsvToDataBaseService csvToDataBaseService;
    private final UserService userService;
    private final AedLocationService aedLocationService;

    @GetMapping("/list")
    public ResponseEntity<?> getAedLoList() {
        try {
            aedService.fetchAllAedData();
            return ResponseEntity.status(HttpStatus.OK).body("completed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
    @PostMapping("/list")
    public ResponseEntity<?> saveAedLoList() {
        try {
            csvToDataBaseService.processCsv("aed_data.csv");
            return ResponseEntity.status(HttpStatus.OK).body("saved completed");
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/within-bounds")
    public ResponseEntity<?> getAedWithinBounds(@RequestBody AedRequestDTO requestDTO) {
        List<AedResponseDTO> responseDTO= aedLocationService.getAedLocation(requestDTO);
        if (responseDTO == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("AED not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }
}
