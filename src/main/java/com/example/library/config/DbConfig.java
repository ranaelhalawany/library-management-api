package com.example.library.config;

import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.model.BorrowingRecord;
import com.example.library.model.Customer;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import com.example.library.repository.BorrowingRecordRepository;
import com.example.library.repository.CustomerRepository;
import com.example.library.service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DbConfig {
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;

    @Bean
    public List<Book> createBooks() {
        Author author1 = new Author();
        author1.setName("J.K. Rowling");
        author1.setBirthDate(LocalDate.of(1965, 7, 31));
        author1.setNationality("British");

        Author author2 = new Author();
        author2.setName("George R.R. Martin");
        author2.setBirthDate(LocalDate.of(1948, 9, 20));
        author2.setNationality("American");

        authorRepository.save(author1);
        authorRepository.save(author2);

        Book book1 = new Book();
        book1.setTitle("Harry Potter and the Philosopher's Stone");
        book1.setAuthor(author1);
        book1.setIsbn("978-0747532699");
        book1.setPublicationDate(LocalDate.of(1997, 6, 26));
        book1.setGenre("Fantasy");
        book1.setAvailable(true);

        Book book2 = new Book();
        book2.setTitle("A Game of Thrones");
        book2.setAuthor(author2);
        book2.setIsbn("978-0553103540");
        book2.setPublicationDate(LocalDate.of(1996, 8, 6));
        book2.setGenre("Fantasy");
        book2.setAvailable(true);

        bookRepository.save(book1);
        bookRepository.save(book2);

        return Arrays.asList(book1, book2);
    }

    @Bean
    public Customer createInitialCustomers() {
        Customer customer1 = new Customer();
        customer1.setName("John Doe");
        customer1.setEmail("john.doe@example.com");
        customer1.setAddress("123 Main St");
        customer1.setPhoneNumber("01111234567");
        customer1.setPassword("password123");

        Customer customer2 = new Customer();
        customer2.setName("Jane Smith");
        customer2.setEmail("jane.smith@example.com");
        customer2.setAddress("456 Elm St");
        customer2.setPhoneNumber("01115000153");
        customer2.setPassword("password456");

        customerService.createCustomer(customer1);
        customerService.createCustomer(customer2);
        return customer1;
    }

    @Bean
    @Transactional
    public Book createBorrowingRecords() {
        Customer customer1 = customerRepository.findById(1L).orElseThrow(() -> new RuntimeException("Customer not found"));
        Customer customer2 = customerRepository.findById(2L).orElseThrow(() -> new RuntimeException("Customer not found"));

        Book book1 = bookRepository.findById(1L).orElseThrow(() -> new RuntimeException("Book not found"));
        Book book2 = bookRepository.findById(2L).orElseThrow(() -> new RuntimeException("Book not found"));

        BorrowingRecord record1 = new BorrowingRecord();
        record1.setCustomer(customer1);
        record1.setBook(book1);
        record1.setBorrowDate(LocalDate.now());
        record1.setReturnDate(LocalDate.now().plusDays(14));

        BorrowingRecord record2 = new BorrowingRecord();
        record2.setCustomer(customer2);
        record2.setBook(book2);
        record2.setBorrowDate(LocalDate.now());
        record2.setReturnDate(LocalDate.now().plusDays(14));

        if (!borrowingRecordRepository.findByCustomerAndBookAndBorrowDate(customer1, book1, LocalDate.now()).isPresent()) {
            borrowingRecordRepository.save(record1);
        }
        if (!borrowingRecordRepository.findByCustomerAndBookAndBorrowDate(customer2, book2, LocalDate.now()).isPresent()) {
            borrowingRecordRepository.save(record2);
        }
        return book1;
    }
}
