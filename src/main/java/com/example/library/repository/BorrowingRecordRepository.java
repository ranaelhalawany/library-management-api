package com.example.library.repository;

import com.example.library.model.Book;
import com.example.library.model.BorrowingRecord;
import com.example.library.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord,Long> {
    List<BorrowingRecord> findByCustomerId(Long userId);
    List<BorrowingRecord> findByBookId(Long bookId);
    Optional<BorrowingRecord> findByCustomerAndBookAndBorrowDate(Customer customer, Book book, LocalDate borrowDate);
    void deleteByBook(Book book);
    void deleteByCustomer(Customer customer);


}
