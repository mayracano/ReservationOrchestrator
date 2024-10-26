package com.example.library.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BookReservationDTO {
    private Long id;
    private Long userId;
    private Long bookId;
}
