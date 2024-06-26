package com.example.library.service;

import com.example.library.event.BookDeleteEvent;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllBooks() {
        List<Book> mockBooks = new ArrayList<>();
        mockBooks.add(new Book(1L, "Book 1", new Author(), "1234567890", LocalDate.of(2020, 1, 1), "genre", true));
        mockBooks.add(new Book(2L, "Book 2", new Author(), "0987654321", LocalDate.of(2019, 5, 10), "genre", true));
        when(bookRepository.findAll()).thenReturn(mockBooks);

        List<Book> result = bookService.getAllBooks();

        assertEquals(2, result.size());
        assertEquals("Book 1", result.get(0).getTitle());
        assertEquals("Book 2", result.get(1).getTitle());
    }

    @Test
    public void testGetBookById_ExistingId() {
        Book mockBook = new Book(1L, "Book 1", new Author(), "1234567890", LocalDate.of(2020, 1, 1), "genre", true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(mockBook));

        Optional<Book> result = bookService.getBookById(1L);

        assertTrue(result.isPresent());
        assertEquals("Book 1", result.get().getTitle());
    }

    @Test
    public void testGetBookById_NonExistingId() {
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookById(2L);

        assertFalse(result.isPresent());
    }

    @Test
    public void testCreateBook_WithExistingAuthor() {
        Author existingAuthor = new Author(1L, "John Doe", LocalDate.of(1980, 5, 15), "American");
        when(authorRepository.findByName("John Doe")).thenReturn(Optional.of(existingAuthor));

        Book inputBook = new Book(null, "New Book", existingAuthor, "0987654321", LocalDate.of(2022, 3, 15), "genre", true);
        Book createdBook = new Book(1L, "New Book", existingAuthor, "0987654321", LocalDate.of(2022, 3, 15), "genre", true);
        when(bookRepository.save(inputBook)).thenReturn(createdBook);

        Book result = bookService.createBook(inputBook);

        assertEquals(1L, result.getId());
        assertEquals("New Book", result.getTitle());
        assertEquals(existingAuthor, result.getAuthor());
        assertEquals("0987654321", result.getIsbn());
        assertEquals(LocalDate.of(2022, 3, 15), result.getPublicationDate());
    }

    @Test
    public void testCreateBook_WithNewAuthor() {
        when(authorRepository.findByName("New Author")).thenReturn(Optional.empty());

        Author inputAuthor = new Author(null, "New Author", LocalDate.of(1990, 3, 25), "French");
        Author createdAuthor = new Author(1L, "New Author", LocalDate.of(1990, 3, 25), "French");
        when(authorRepository.save(inputAuthor)).thenReturn(createdAuthor);

        Book inputBook = new Book(null, "New Book", inputAuthor, "0987654321", LocalDate.of(2022, 3, 15), "genre", true);
        Book createdBook = new Book(1L, "New Book", createdAuthor, "0987654321", LocalDate.of(2022, 3, 15), "genre", true);
        when(bookRepository.save(inputBook)).thenReturn(createdBook);

        Book result = bookService.createBook(inputBook);

        assertEquals(1L, result.getId());
        assertEquals("New Book", result.getTitle());
        assertEquals(createdAuthor, result.getAuthor());
        assertEquals("0987654321", result.getIsbn());
        assertEquals(LocalDate.of(2022, 3, 15), result.getPublicationDate());
    }

    @Test
    public void testUpdateBook_ExistingId() {
        Book existingBook = new Book(1L, "Book 1", new Author(), "1234567890", LocalDate.of(2020, 1, 1), "genre", true);
        Book updatedBook = new Book(1L, "Updated Book", new Author(), "0987654321", LocalDate.of(2022, 5, 20), "genre", true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(updatedBook);

        Optional<Book> result = bookService.updateBook(1L, updatedBook);

        assertTrue(result.isPresent());
        assertEquals("Updated Book", result.get().getTitle());
        assertEquals("0987654321", result.get().getIsbn());
        assertEquals(LocalDate.of(2022, 5, 20), result.get().getPublicationDate());
    }

    @Test
    public void testUpdateBook_NonExistingId() {
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.updateBook(2L, new Book());

        assertFalse(result.isPresent());
    }

    @Test
    public void testDeleteBook_ExistingId() {
        Book existingBook = new Book(1L, "Book 1", new Author(), "1234567890", LocalDate.of(2020, 1, 1), "genre", true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existingBook));

        boolean result = bookService.deleteBook(1L);

        assertTrue(result);
        verify(eventPublisher, times(1)).publishEvent(any(BookDeleteEvent.class));
        verify(bookRepository, times(1)).delete(existingBook);
    }

    @Test
    public void testDeleteBook_NonExistingId() {
        when(bookRepository.findById(2L)).thenReturn(Optional.empty());

        boolean result = bookService.deleteBook(2L);

        assertFalse(result);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    public void testDeleteBook_UnavailableBook() {
        Book unavailableBook = new Book();
        unavailableBook.setId(1L);
        unavailableBook.setAvailable(false);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(unavailableBook));

        assertThrows(IllegalArgumentException.class, () -> bookService.deleteBook(1L), "The book is currently borrowed and cannot be deleted.");

        verify(bookRepository, never()).delete(unavailableBook);
        verify(eventPublisher, never()).publishEvent(any(BookDeleteEvent.class));
    }

    @Test
    public void testDeleteBook_AvailableBook() {
        Book availableBook = new Book();
        availableBook.setId(1L);
        availableBook.setAvailable(true);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(availableBook));

        boolean result = bookService.deleteBook(1L);

        assertTrue(result);
        verify(bookRepository, times(1)).delete(availableBook);
        verify(eventPublisher, times(1)).publishEvent(any(BookDeleteEvent.class));
    }

    @Test
    public void testSearchBooksByTitle() {
        List<Book> mockBooks = new ArrayList<>();
        mockBooks.add(new Book(1L, "Book 1", new Author(), "1234567890", LocalDate.of(2020, 1, 1), "genre", true));
        mockBooks.add(new Book(2L, "Book 2", new Author(), "0987654321", LocalDate.of(2019, 5, 10), "genre", true));
        when(bookRepository.findByTitleContaining("Book")).thenReturn(mockBooks);

        List<Book> result = bookService.searchBooksByTitle("Book");

        assertEquals(2, result.size());
        assertEquals("Book 1", result.get(0).getTitle());
        assertEquals("Book 2", result.get(1).getTitle());
    }

    @Test
    public void testSearchBooksByAuthor() {
        List<Book> mockBooks = new ArrayList<>();
        Author author = new Author(1L, "John Doe", LocalDate.of(1980, 5, 15), "American");
        mockBooks.add(new Book(1L, "Book 1", author, "1234567890", LocalDate.of(2020, 1, 1), "genre", true));
        when(bookRepository.findByAuthor_NameContaining("John")).thenReturn(mockBooks);

        List<Book> result = bookService.searchBooksByAuthor("John");

        assertEquals(1, result.size());
        assertEquals("Book 1", result.get(0).getTitle());
        assertEquals(author, result.get(0).getAuthor());
    }

    @Test
    public void testSearchBooksByIsbn() {
        List<Book> mockBooks = new ArrayList<>();
        mockBooks.add(new Book(1L, "Book 1", new Author(), "1234567890", LocalDate.of(2020, 1, 1), "genre", true));
        when(bookRepository.findByIsbnContaining("123")).thenReturn(mockBooks);

        List<Book> result = bookService.searchBooksByIsbn("123");

        assertEquals(1, result.size());
        assertEquals("Book 1", result.get(0).getTitle());
        assertEquals("1234567890", result.get(0).getIsbn());
    }
}
