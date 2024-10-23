package com.example.PushOfLife.controller;

import com.example.PushOfLife.service.AedService;
import com.example.PushOfLife.service.CsvToDataBaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aed")
@RequiredArgsConstructor
public class AedController {

    private final AedService aedService;
    private final CsvToDataBaseService csvToDataBaseService;

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
}
