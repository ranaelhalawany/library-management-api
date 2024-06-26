package com.example.library.controller;

import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    public void testGetAllBooks_ValidRequest_Success() throws Exception {
        Author author1 = new Author();
        author1.setId(1L);
        author1.setName("John Doe");
        author1.setBirthDate(LocalDate.of(1965, 7, 31));
        author1.setNationality("British");

        Author author2 = new Author();
        author2.setId(2L);
        author2.setName("Jane Smith");
        author2.setBirthDate(LocalDate.of(1948, 9, 20));
        author2.setNationality("American");

        Book book1 = new Book(1L, "Book 1", author1, "1234567890", LocalDate.of(2020, 1, 1), "Fiction", true);
        Book book2 = new Book(2L, "Book 2", author2, "0987654321", LocalDate.of(2018, 5, 15), "Non-fiction", false);
        List<Book> books = Arrays.asList(book1, book2);
        Mockito.when(bookService.getAllBooks()).thenReturn(books);


        ResultActions result = mockMvc.perform(get("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON));


        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Book 1"))
                .andExpect(jsonPath("$[1].title").value("Book 2"));
    }

    @Test
    public void testGetBookById_ExistingId_Success() throws Exception {

        Author author = new Author();
        author.setId(1L);
        author.setName("John Doe");
        Book book = new Book(1L, "Book 1", author, "1234567890", LocalDate.of(2020, 1, 1), "Fiction", true);
        Mockito.when(bookService.getBookById(1L)).thenReturn(Optional.of(book));

        ResultActions result = mockMvc.perform(get("/api/v1/books/1")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Book 1"));
    }

    @Test
    public void testGetBookById_NonExistingId_NotFound() throws Exception {
        Mockito.when(bookService.getBookById(2L)).thenReturn(Optional.empty());

        ResultActions result = mockMvc.perform(get("/api/v1/books/2")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testCreateBook_ValidBook_Success() throws Exception {
        Author author = new Author();
        author.setId(1L);
        author.setName("John Doe");
        Book inputBook = new Book(null, "New Book", author, "0987654321", LocalDate.of(2021, 3, 10), "Mystery", true);
        Book createdBook = new Book(1L, "New Book", author, "0987654321", LocalDate.of(2021, 3, 10), "Mystery", true);
        Mockito.when(bookService.createBook(ArgumentMatchers.any(Book.class))).thenReturn(createdBook);

        ResultActions result = mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"New Book\", \"author\": {\"id\": 1, \"name\": \"John Doe\"}, \"isbn\": \"0987654321\", \"publicationDate\": \"2021-03-10\", \"genre\": \"Mystery\", \"available\": true}"));

        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("New Book"));
    }

    @Test
    public void testUpdateBook_ExistingId_ValidBook_Success() throws Exception {
        Author author = new Author();
        author.setId(1L);
        author.setName("John Doe");
        Book updatedBook = new Book(1L, "Updated Book", author, "1234567890", LocalDate.of(2022, 2, 5), "Thriller", true);
        Mockito.when(bookService.updateBook(ArgumentMatchers.eq(1L), ArgumentMatchers.any(Book.class)))
                .thenReturn(Optional.of(updatedBook));

        ResultActions result = mockMvc.perform(put("/api/v1/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Updated Book\", \"author\": {\"id\": 1, \"name\": \"John Doe\"}, \"isbn\": \"1234567890\", \"publicationDate\": \"2022-02-05\", \"genre\": \"Thriller\", \"available\": true}"));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("Updated Book"));
    }

    @Test
    public void testUpdateBook_NonExistingId_NotFound() throws Exception {
        Mockito.when(bookService.updateBook(ArgumentMatchers.eq(2L), ArgumentMatchers.any(Book.class)))
                .thenReturn(Optional.empty());

        ResultActions result = mockMvc.perform(put("/api/v1/books/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\": \"Updated Book\", \"author\": {\"id\": 1, \"name\": \"John Doe\"}, \"isbn\": \"1234567890\", \"publicationDate\": \"2022-02-05\", \"genre\": \"Thriller\", \"available\": true}"));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteBook_ExistingId_Success() throws Exception {

        Mockito.when(bookService.deleteBook(1L)).thenReturn(true);


        ResultActions result = mockMvc.perform(delete("/api/v1/books/1"));


        result.andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteBook_NonExistingId_NotFound() throws Exception {

        Mockito.when(bookService.deleteBook(2L)).thenReturn(false);


        ResultActions result = mockMvc.perform(delete("/api/v1/books/2"));


        result.andExpect(status().isNotFound());
    }

    @Test
    public void testSearchBooks_ByTitle_Success() throws Exception {

        Author author = new Author();
        author.setId(1L);
        author.setName("John Doe");
        Book book1 = new Book(1L, "Book 1", author, "1234567890", LocalDate.of(2020, 1, 1), "Fiction", true);
        Book book2 = new Book(2L, "Book 2", author, "0987654321", LocalDate.of(2018, 5, 15), "Non-fiction", false);
        List<Book> books = Arrays.asList(book1, book2);
        Mockito.when(bookService.searchBooksByTitle("Book")).thenReturn(books);


        ResultActions result = mockMvc.perform(get("/api/v1/books/search?title=Book")
                .contentType(MediaType.APPLICATION_JSON));


        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").value("Book 1"))
                .andExpect(jsonPath("$[1].title").value("Book 2"));
    }


    private EntityModel<Book> toBookModel(Book book) {


        return EntityModel.of(book);
    }
}