package com.example.library.dto;

public class BookReservationEvent {
    private BookReservationDTO bookReservationDTO;
    private BookReservationStatus bookReservationStatus;

    public BookReservationDTO getBookReservation() {
        return bookReservationDTO;
    }

    public void setBookReservation(BookReservationDTO bookReservationDTO) {
        this.bookReservationDTO = bookReservationDTO;
    }

    public BookReservationStatus getBookReservationStatus() {
        return bookReservationStatus;
    }

    public void setBookReservationStatus(BookReservationStatus bookReservationStatus) {
        this.bookReservationStatus = bookReservationStatus;
    }
}
