package com.example.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(force = true)
@AllArgsConstructor
@Setter
@Getter
public class BorrowingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "userId")
    @NonNull
    private Customer customer;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})

    @JoinColumn(name = "bookId")
    @NonNull
    private Book book;
    @NonNull
    @PastOrPresent
    private LocalDate borrowDate;
    @NonNull
    @FutureOrPresent
    private LocalDate returnDate;
}
