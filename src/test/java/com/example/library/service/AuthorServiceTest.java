package com.example.library.service;

import com.example.library.event.AuthorDeleteEvent;
import com.example.library.model.Author;
import com.example.library.repository.AuthorRepository;
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

public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthorService authorService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllAuthors() {
        List<Author> mockAuthors = new ArrayList<>();
        mockAuthors.add(new Author(1L, "John Doe", LocalDate.of(1980, 5, 15), "American"));
        mockAuthors.add(new Author(2L, "Jane Smith", LocalDate.of(1975, 8, 21), "British"));
        when(authorRepository.findAll()).thenReturn(mockAuthors);

        List<Author> result = authorService.getAllAuthors();


        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
    }

    @Test
    public void testGetAuthorById_ExistingId() {
        Author mockAuthor = new Author(1L, "John Doe", LocalDate.of(1980, 5, 15), "American");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(mockAuthor));

        Optional<Author> result = authorService.getAuthorById(1L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testGetAuthorById_NonExistingId() {
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Author> result = authorService.getAuthorById(2L);

        assertFalse(result.isPresent());
    }

    @Test
    public void testCreateAuthor() {
        Author inputAuthor = new Author(null, "New Author", LocalDate.of(1990, 3, 25), "French");
        Author createdAuthor = new Author(1L, "New Author", LocalDate.of(1990, 3, 25), "French");
        when(authorRepository.save(inputAuthor)).thenReturn(createdAuthor);

        Author result = authorService.createAuthor(inputAuthor);


        assertEquals(1L, result.getId());
        assertEquals("New Author", result.getName());
        assertEquals(LocalDate.of(1990, 3, 25), result.getBirthDate());
        assertEquals("French", result.getNationality());
    }

    @Test
    public void testUpdateAuthor_ExistingId() {
        Author existingAuthor = new Author(1L, "John Doe", LocalDate.of(1980, 5, 15), "American");
        Author updatedAuthor = new Author(1L, "Updated Author", LocalDate.of(1985, 10, 30), "Canadian");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(existingAuthor));
        when(authorRepository.save(existingAuthor)).thenReturn(updatedAuthor);

        Optional<Author> result = authorService.updateAuthor(1L, updatedAuthor);

        assertTrue(result.isPresent());
        assertEquals("Updated Author", result.get().getName());
        assertEquals(LocalDate.of(1985, 10, 30), result.get().getBirthDate());
        assertEquals("Canadian", result.get().getNationality());
    }

    @Test
    public void testUpdateAuthor_NonExistingId() {
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Author> result = authorService.updateAuthor(2L, new Author());

        assertFalse(result.isPresent());
    }

    @Test
    public void testDeleteAuthor_ExistingId() {
        Author existingAuthor = new Author(1L, "John Doe", LocalDate.of(1980, 5, 15), "American");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(existingAuthor));

        boolean result = authorService.deleteAuthor(1L);

        assertTrue(result);
        verify(eventPublisher, times(1)).publishEvent(any(AuthorDeleteEvent.class));
        verify(authorRepository, times(1)).delete(existingAuthor);
    }

    @Test
    public void testDeleteAuthor_NonExistingId() {
        when(authorRepository.findById(2L)).thenReturn(Optional.empty());

        boolean result = authorService.deleteAuthor(2L);

        assertFalse(result);
        verify(authorRepository, never()).delete(any(Author.class));
    }

    @Test
    public void testParseAndEditBirthdate_ValidFormat() {
        LocalDate result = authorService.parseAndEditBirthdate("2024-06-23");

        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 6, 24), result);
    }

    @Test
    public void testParseAndEditBirthdate_InvalidFormat() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authorService.parseAndEditBirthdate("23-06-2024");
        });

        assertEquals("Invalid date format, expected yyyy-MM-dd", exception.getMessage());
    }
}
