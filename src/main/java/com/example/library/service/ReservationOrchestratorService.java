package com.example.library.service;

import com.example.library.dto.BookReservationDTO;
import com.example.library.dto.BookReservationEvent;
import com.example.library.dto.BookReservationStatus;
import com.example.library.model.BookReservation;
import com.example.library.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReservationOrchestratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationOrchestratorService.class);

    @Autowired
    KafkaTemplate<String, BookReservationEvent> kafkaTemplate;

    @Autowired
    ReservationRepository reservationRepository;

    public void createReservation(BookReservationDTO bookReservationDTO) {
        addReservation(bookReservationDTO);
        removeFromInventory(bookReservationDTO);
    }

    @KafkaListener(topics = "reversed-reservations", groupId = "reservations-group")
    public void reverseAddReservation(String event) throws Exception {
        BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
        LOGGER.info(String.format("Operation to register Book reservation Failed for: %s and book %s", bookReservationEvent.getBookReservation().getBookId(), bookReservationEvent.getBookReservation().getUserId()));
        kafkaTemplate.send("reverse-inventory", bookReservationEvent);
        reservationRepository.findById(bookReservationEvent.getBookReservation().getId())
                .ifPresent(this::updateReservationStatusFailed);
    }

    @KafkaListener(topics = "completed-reservations", groupId = "reservations-group")
    public void completeReservation(String event) throws Exception {
        BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
        LOGGER.info(String.format("Operation to register Book Reservation Completed for: %s and book %s", bookReservationEvent.getBookReservation().getBookId(), bookReservationEvent.getBookReservation().getUserId()));
        reservationRepository.findById(bookReservationEvent.getBookReservation().getId())
                .ifPresent(this::updateReservationStatusOK);
    }

    @KafkaListener(topics = "reversed-inventory", groupId = "reservations-group")
    public void reverseInventory(String event) throws Exception {
        BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
        LOGGER.info(String.format("Operation to add inventory Failed for: %s and book %s", bookReservationEvent.getBookReservation().getBookId(), bookReservationEvent.getBookReservation().getUserId()));
        kafkaTemplate.send("reverse-reservations", bookReservationEvent);
        reservationRepository.findById(bookReservationEvent.getBookReservation().getId())
                .ifPresent(this::updateReservationStatusFailed);
    }

    @KafkaListener(topics = "completed-inventory", groupId = "reservations-group")
    public void completeInventory(String event) throws Exception {
        BookReservationEvent bookReservationEvent = new ObjectMapper().readValue(event, BookReservationEvent.class);
        LOGGER.info(String.format("Operation to add inventory completed for: %s and book %s", bookReservationEvent.getBookReservation().getBookId(), bookReservationEvent.getBookReservation().getUserId()));
        reservationRepository.findByBookIdAndUserId(bookReservationEvent.getBookReservation().getBookId(), bookReservationEvent.getBookReservation().getUserId())
                .ifPresent(this::updateReservationStatusOK);
    }

    private void removeFromInventory(BookReservationDTO bookReservationDTO) {
        BookReservationEvent bookReservationInventoryEvent = new BookReservationEvent();
        bookReservationInventoryEvent.setBookReservation(bookReservationDTO);
        bookReservationInventoryEvent.setBookReservationStatus(BookReservationStatus.CREATED);
        kafkaTemplate.send("add-inventory", bookReservationInventoryEvent);
        LOGGER.info(String.format("Inventory removed for user: %s and book: %s", bookReservationDTO.getBookId(), bookReservationDTO.getUserId()));
    }

    private void addReservation(BookReservationDTO bookReservationDTO) {
        saveReservationStatus(bookReservationDTO);

        BookReservationEvent bookReservationEvent = new BookReservationEvent();
        bookReservationEvent.setBookReservation(bookReservationDTO);
        bookReservationEvent.setBookReservationStatus(BookReservationStatus.CREATED);
        kafkaTemplate.send("new-reservation", bookReservationEvent);
        LOGGER.info(String.format("Reservation created for user: %s and book: %s", bookReservationDTO.getBookId(), bookReservationDTO.getUserId()));

    }

    private void saveReservationStatus(BookReservationDTO bookReservationDTO) {
        BookReservation bookReservation = new BookReservation();
        bookReservation.setBookId(bookReservationDTO.getBookId());
        bookReservation.setUserId(bookReservationDTO.getUserId());
        bookReservation.setReservationId(bookReservationDTO.getId());
        bookReservation.setReservationStatus(BookReservationStatus.PENDING);
        reservationRepository.save(bookReservation);
    }

    private void updateReservationStatusOK(BookReservation bookReservation) {
        if (bookReservation.getReservationStatus().equals(BookReservationStatus.PENDING)) {
            bookReservation.setReservationStatus(BookReservationStatus.CREATED);
        } else if (bookReservation.getReservationStatus().equals(BookReservationStatus.CREATED)) {
            bookReservation.setReservationStatus(BookReservationStatus.COMPLETED);
        }
    }

    private void updateReservationStatusFailed(BookReservation bookReservation) {
        if (bookReservation.getReservationStatus().equals(BookReservationStatus.PENDING)) {
            bookReservation.setReservationStatus(BookReservationStatus.REVERSED);
        } else if (bookReservation.getReservationStatus().equals(BookReservationStatus.REVERSED)) {
            bookReservation.setReservationStatus(BookReservationStatus.CANCELLED);
        }
    }
}
