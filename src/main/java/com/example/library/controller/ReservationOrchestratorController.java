package com.example.library.controller;

import com.example.library.dto.BookReservationDTO;
import com.example.library.service.ReservationOrchestratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/reservations/")
public class ReservationOrchestratorController {

    @Autowired
    ReservationOrchestratorService reservationOrchestratorService;

    @PostMapping
    public ResponseEntity<String> createBookReservation(@RequestBody BookReservationDTO bookReservationDTO) {
        reservationOrchestratorService.createReservation(bookReservationDTO);
        return new ResponseEntity<>("Book Reservation Generated", HttpStatus.OK);
    }
}
