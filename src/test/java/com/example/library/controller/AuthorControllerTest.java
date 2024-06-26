package com.example.library.controller;

import com.example.library.model.Author;
import com.example.library.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthorController.class)
public class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @Test
    public void testGetAllAuthors_ValidRequest_Success() throws Exception {
        Author author1 = new Author();
        author1.setName("J.K. Rowling");
        author1.setBirthDate(LocalDate.of(1965, 7, 31));
        author1.setNationality("British");

        Author author2 = new Author();
        author2.setName("George R.R. Martin");
        author2.setBirthDate(LocalDate.of(1948, 9, 20));
        author2.setNationality("American");

        List<Author> authors = Arrays.asList(author1, author2);
        Mockito.when(authorService.getAllAuthors()).thenReturn(authors);

        ResultActions result = mockMvc.perform(get("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("J.K. Rowling"))
                .andExpect(jsonPath("$[1].name").value("George R.R. Martin"));
    }

    @Test
    public void testGetAuthorById_ExistingId_Success() throws Exception {
        Author author = new Author();
        author.setId(1L);
        author.setName("J.K. Rowling");
        Mockito.when(authorService.getAuthorById(1L)).thenReturn(Optional.of(author));

        ResultActions result = mockMvc.perform(get("/api/v1/authors/1")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("J.K. Rowling"));
    }

    @Test
    public void testGetAuthorById_NonExistingId_NotFound() throws Exception {
        Mockito.when(authorService.getAuthorById(2L)).thenReturn(Optional.empty());

        ResultActions result = mockMvc.perform(get("/api/v1/authors/2")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testCreateAuthor_ValidAuthor_Success() throws Exception {
        Author inputAuthor = new Author();
        inputAuthor.setId(null);
        inputAuthor.setName("New Author");
        Author createdAuthor = new Author();
        createdAuthor.setId(1L);
        createdAuthor.setName("New Author");
        Mockito.when(authorService.createAuthor(ArgumentMatchers.any(Author.class))).thenReturn(createdAuthor);

        ResultActions result = mockMvc.perform(post("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"New Author\"}"));

        result.andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Author"));
    }

    @Test
    public void testUpdateAuthor_ExistingId_ValidAuthor_Success() throws Exception {
        Author updatedAuthor = new Author();
        updatedAuthor.setId(1L);
        updatedAuthor.setName("Updated Author");
        Mockito.when(authorService.updateAuthor(ArgumentMatchers.eq(1L), ArgumentMatchers.any(Author.class)))
                .thenReturn(Optional.of(updatedAuthor));

        ResultActions result = mockMvc.perform(put("/api/v1/authors/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Author\"}"));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Author"));
    }

    @Test
    public void testUpdateAuthor_NonExistingId_NotFound() throws Exception {
        Mockito.when(authorService.updateAuthor(ArgumentMatchers.eq(2L), ArgumentMatchers.any(Author.class)))
                .thenReturn(Optional.empty());

        ResultActions result = mockMvc.perform(put("/api/v1/authors/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Author\"}"));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteAuthor_ExistingId_Success() throws Exception {
        Mockito.when(authorService.deleteAuthor(1L)).thenReturn(true);

        ResultActions result = mockMvc.perform(delete("/api/v1/authors/1"));

        result.andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteAuthor_NonExistingId_NotFound() throws Exception {
        Mockito.when(authorService.deleteAuthor(2L)).thenReturn(false);

        ResultActions result = mockMvc.perform(delete("/api/v1/authors/2"));

        result.andExpect(status().isNotFound());
    }

}
