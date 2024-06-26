package com.example.library.service;

import com.example.library.exception.BookAlreadyBorrowedException;
import com.example.library.exception.BookNotFoundException;
import com.example.library.exception.BorrowingRecordAlreadyExistsException;
import com.example.library.exception.CustomerNotFoundException;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.model.BorrowingRecord;
import com.example.library.model.Customer;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowingRecordRepository;
import com.example.library.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BorrowingRecordServiceTest {

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BorrowingRecordService borrowingRecordService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllBorrowingRecords() {
        List<BorrowingRecord> borrowingRecords = new ArrayList<>();
        borrowingRecords.add(new BorrowingRecord(1L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(14)));
        borrowingRecords.add(new BorrowingRecord(2L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(7)));
        when(borrowingRecordRepository.findAll()).thenReturn(borrowingRecords);

        List<BorrowingRecord> result = borrowingRecordService.getAllBorrowingRecords();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(LocalDate.now(), result.get(1).getBorrowDate());
    }

    @Test
    public void testGetBorrowingRecordById_ExistingId() {
        BorrowingRecord borrowingRecord = new BorrowingRecord(1L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(14));
        when(borrowingRecordRepository.findById(1L)).thenReturn(Optional.of(borrowingRecord));

        Optional<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    public void testGetBorrowingRecordById_NonExistingId() {
        when(borrowingRecordRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<BorrowingRecord> result = borrowingRecordService.getBorrowingRecordById(2L);

        assertFalse(result.isPresent());
    }

    @Test
    public void testCreateBorrowingRecord_ValidBorrowingRecord() {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123");
        Book book = new Book(1L, "Book Title", new Author(), "1234567890", LocalDate.now(), "Genre", true);
        BorrowingRecord borrowingRecord = new BorrowingRecord(null, customer, book, LocalDate.now(), LocalDate.now().plusDays(14));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(borrowingRecordRepository.save(borrowingRecord)).thenReturn(borrowingRecord);

        BorrowingRecord result = borrowingRecordService.createBorrowingRecord(borrowingRecord);

        assertNotNull(result);
        assertEquals(1L, result.getCustomer().getId());
        assertEquals("Book Title", result.getBook().getTitle());
    }

    @Test
    public void testCreateBorrowingRecord_CustomerNotFound() {
        Book book = new Book(1L, "Book Title", new Author(), "1234567890", LocalDate.now(), "Genre", true);
        BorrowingRecord borrowingRecord = new BorrowingRecord(null, new Customer(), book, LocalDate.now(), LocalDate.now().plusDays(14));
        when(customerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> borrowingRecordService.createBorrowingRecord(borrowingRecord));
    }

    @Test
    public void testCreateBorrowingRecord_BookNotFound() {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123");
        BorrowingRecord borrowingRecord = new BorrowingRecord(null, customer, new Book(), LocalDate.now(), LocalDate.now().plusDays(14));
        when(customerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(customer));
        when(bookRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> borrowingRecordService.createBorrowingRecord(borrowingRecord));
    }

    @Test
    public void testCreateBorrowingRecord_BookAlreadyBorrowed() {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123");
        Book book = new Book(1L, "Book Title", new Author(), "1234567890", LocalDate.now(), "Genre", false);
        BorrowingRecord borrowingRecord = new BorrowingRecord(null, customer, book, LocalDate.now(), LocalDate.now().plusDays(14));
        when(customerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(customer));
        when(bookRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(book));

        assertThrows(BookAlreadyBorrowedException.class, () -> borrowingRecordService.createBorrowingRecord(borrowingRecord));
    }

    @Test
    public void testCreateBorrowingRecord_RecordAlreadyExists() {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123");
        Book book = new Book(1L, "Book Title", new Author(), "1234567890", LocalDate.now(), "Genre", true);
        BorrowingRecord existingRecord = new BorrowingRecord(1L, customer, book, LocalDate.now(), LocalDate.now().plusDays(14));
        BorrowingRecord newRecord = new BorrowingRecord(null, customer, book, LocalDate.now(), LocalDate.now().plusDays(14));
        when(customerRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(customer));
        when(bookRepository.findById(ArgumentMatchers.anyLong())).thenReturn(Optional.of(book));
        when(borrowingRecordRepository.findByCustomerAndBookAndBorrowDate(customer, book, newRecord.getBorrowDate())).thenReturn(Optional.of(existingRecord));

        assertThrows(BorrowingRecordAlreadyExistsException.class, () -> borrowingRecordService.createBorrowingRecord(newRecord));
    }

    @Test
    public void testUpdateBorrowingRecord_ExistingId_ValidBorrowingRecord() {
        BorrowingRecord updatedRecord = new BorrowingRecord(1L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(14));
        when(borrowingRecordRepository.findById(1L)).thenReturn(Optional.of(updatedRecord));
        when(borrowingRecordRepository.save(updatedRecord)).thenReturn(updatedRecord);

        Optional<BorrowingRecord> result = borrowingRecordService.updateBorrowingRecord(1L, updatedRecord);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    public void testUpdateBorrowingRecord_NonExistingId() {
        when(borrowingRecordRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<BorrowingRecord> result = borrowingRecordService.updateBorrowingRecord(2L, new BorrowingRecord());

        assertFalse(result.isPresent());
    }

    @Test
    public void testDeleteBorrowingRecord_ExistingId() {

        Book book = new Book(1L, "Book 1", new Author(), "1234567890", LocalDate.of(2020, 1, 1), "genre", true);

        BorrowingRecord borrowingRecord = new BorrowingRecord(1L, new Customer(), book, LocalDate.now().minusDays(14), LocalDate.now().minusDays(10));
        when(borrowingRecordRepository.findById(1L)).thenReturn(Optional.of(borrowingRecord));
        when(borrowingRecordRepository.existsById(1L)).thenReturn(true);

        boolean result = borrowingRecordService.deleteBorrowingRecord(1L);

        assertTrue(result);
        verify(borrowingRecordRepository, times(1)).deleteById(1L);
    }


    @Test
    public void testDeleteBorrowingRecord_NonExistingId() {
        when(borrowingRecordRepository.findById(2L)).thenReturn(Optional.empty());

        boolean result = borrowingRecordService.deleteBorrowingRecord(2L);

        assertFalse(result);
        verify(borrowingRecordRepository, never()).deleteById(2L);
    }

    @Test
    public void testDeleteBorrowingRecord_ReturnDateInFuture() {


        Book book = new Book(1L, "Book 1", new Author(), "1234567890", LocalDate.of(2020, 1, 1), "genre", true);

        BorrowingRecord borrowingRecord = new BorrowingRecord(1L, new Customer(), book, LocalDate.now().minusDays(14), LocalDate.now().plusDays(10));
        when(borrowingRecordRepository.findById(1L)).thenReturn(Optional.of(borrowingRecord));
        when(borrowingRecordRepository.existsById(1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> borrowingRecordService.deleteBorrowingRecord(1L), "The return date is still in the future, and the borrowing record cannot be deleted.");


    }

    @Test
    public void testFindBorrowingRecordsByUserId() {
        List<BorrowingRecord> borrowingRecords = new ArrayList<>();
        borrowingRecords.add(new BorrowingRecord(1L, new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123"), new Book(), LocalDate.now(), LocalDate.now().plusDays(14)));
        borrowingRecords.add(new BorrowingRecord(2L, new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123"), new Book(), LocalDate.now(), LocalDate.now().plusDays(7)));
        when(borrowingRecordRepository.findByCustomerId(1L)).thenReturn(borrowingRecords);

        List<BorrowingRecord> result = borrowingRecordService.findBorrowingRecordsByUserId(1L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("john@example.com", result.get(1).getCustomer().getEmail());
    }

    @Test
    public void testFindBorrowingRecordsByBookId() {
        List<BorrowingRecord> borrowingRecords = new ArrayList<>();
        borrowingRecords.add(new BorrowingRecord(1L, new Customer(), new Book(1L, "Book Title", new Author(), "1234567890", LocalDate.now(), "Genre", true), LocalDate.now(), LocalDate.now().plusDays(14)));
        borrowingRecords.add(new BorrowingRecord(2L, new Customer(), new Book(1L, "Book Title", new Author(), "1234567890", LocalDate.now(), "Genre", true), LocalDate.now(), LocalDate.now().plusDays(7)));
        when(borrowingRecordRepository.findByBookId(1L)).thenReturn(borrowingRecords);

        List<BorrowingRecord> result = borrowingRecordService.findBorrowingRecordsByBookId(1L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Book Title", result.get(1).getBook().getTitle());
    }
}
