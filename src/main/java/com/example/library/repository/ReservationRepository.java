package com.example.library.repository;

import java.util.Optional;

import com.example.library.model.BookReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<BookReservation, Long> {

    Optional<BookReservation> findByBookIdAndUserId(Long bookId, Long userId);

}
