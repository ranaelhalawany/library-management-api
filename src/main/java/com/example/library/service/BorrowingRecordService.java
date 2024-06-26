package com.example.library.service;

import com.example.library.exception.BookAlreadyBorrowedException;
import com.example.library.exception.BookNotFoundException;
import com.example.library.exception.BorrowingRecordAlreadyExistsException;
import com.example.library.exception.CustomerNotFoundException;
import com.example.library.model.Book;
import com.example.library.model.BorrowingRecord;
import com.example.library.model.Customer;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowingRecordRepository;
import com.example.library.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BorrowingRecordService {

    private final BorrowingRecordRepository borrowingRecordRepository;
    private final CustomerRepository customerRepository;
    private final BookRepository bookRepository;

    public List<BorrowingRecord> getAllBorrowingRecords() {
        return borrowingRecordRepository.findAll();
    }

    public Optional<BorrowingRecord> getBorrowingRecordById(Long id) {
        return borrowingRecordRepository.findById(id);
    }


    @Transactional
    public BorrowingRecord createBorrowingRecord(BorrowingRecord borrowingRecord) {
        try {
            Optional<Customer> customer = customerRepository.findById(borrowingRecord.getCustomer().getId());
            if (customer.isEmpty()) {
                throw new CustomerNotFoundException("Customer does not exist");
            }

            Optional<Book> book = bookRepository.findById(borrowingRecord.getBook().getId());
            if (book.isEmpty()) {
                throw new BookNotFoundException("Book does not exist");
            }
            if (!book.get().isAvailable()) {
                throw new BookAlreadyBorrowedException("Book is already borrowed");
            }

            Optional<BorrowingRecord> existingRecord = borrowingRecordRepository.findByCustomerAndBookAndBorrowDate(
                    customer.get(), book.get(), borrowingRecord.getBorrowDate());
            if (existingRecord.isPresent()) {
                throw new BorrowingRecordAlreadyExistsException("Borrowing record with the same customer, book, and borrow date already exists");
            }
            book.get().setAvailable(false);
            borrowingRecord.setCustomer(customer.get());
            borrowingRecord.setBook(book.get());

            return borrowingRecordRepository.save(borrowingRecord);

        } catch (ConstraintViolationException e) {
            Set<ConstraintViolation<?>> violations = e.getConstraintViolations();

            StringBuilder errorMessage = new StringBuilder();

            for (ConstraintViolation<?> violation : violations) {
                errorMessage.append(" ").append(violation.getPropertyPath()).append(": ").append(violation.getMessage());
            }

            throw new IllegalArgumentException(errorMessage.toString());
        }
    }

    public Optional<BorrowingRecord> updateBorrowingRecord(Long id, BorrowingRecord updatedBorrowingRecord) {
        return borrowingRecordRepository.findById(id).map(existingRecord -> {
            existingRecord.setCustomer(updatedBorrowingRecord.getCustomer());
            existingRecord.setBook(updatedBorrowingRecord.getBook());
            existingRecord.setBorrowDate(updatedBorrowingRecord.getBorrowDate());
            existingRecord.setReturnDate(updatedBorrowingRecord.getReturnDate());
            return borrowingRecordRepository.save(existingRecord);
        });
    }

    @Transactional
    public boolean deleteBorrowingRecord(Long id) {
        if (borrowingRecordRepository.existsById(id)) {
            BorrowingRecord borrowingRecord = borrowingRecordRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Borrowing record not found"));

            if (borrowingRecord.getReturnDate().isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("The return date is still in the future, and the borrowing record cannot be deleted.");
            }

            borrowingRecordRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }



    public List<BorrowingRecord> findBorrowingRecordsByUserId(Long userId) {
        return borrowingRecordRepository.findByCustomerId(userId);
    }

    public List<BorrowingRecord> findBorrowingRecordsByBookId(Long bookId) {
        return borrowingRecordRepository.findByBookId(bookId);
    }
}
