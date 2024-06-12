package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Controller", description = "API for managing books")
public class BookController {

    private final BookService bookService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all books", description = "Retrieve a list of all books")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Book.class))))
    })
    public ResponseEntity<?> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        List<EntityModel<Book>> bookModels = books.stream()
                .map(this::toBookModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookModels);
    }

    @Operation(summary = "Get book by ID", description = "Retrieve a specific book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved book",
                    content = @Content(schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)

    public ResponseEntity<EntityModel<Book>> getBookById(@PathVariable Long id) {
        Optional<Book> bookOptional = bookService.getBookById(id);

        return bookOptional.map(value -> ResponseEntity.ok(toBookModel(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new book", description = "Create a new book")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created book",
                    content = @Content(schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Book>> createBook(@Valid @RequestBody Book book) {
        Book createdBook = bookService.createBook(book);
        return ResponseEntity.created(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BookController.class)
                        .getBookById(createdBook.getId())).toUri())
                .body(toBookModel(createdBook));
    }

    @Operation(summary = "Update an existing book", description = "Update an existing book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated book",
                    content = @Content(schema = @Schema(implementation = Book.class))),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Book>> updateBook(@PathVariable Long id, @Valid @RequestBody Book updatedBook) {
        Optional<Book> book = bookService.updateBook(id, updatedBook);

        return book.map(value -> {
            EntityModel<Book> bookModel = toBookModel(value);
            return ResponseEntity.ok(bookModel);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a book", description = "Delete a book by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted book"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        if (bookService.deleteBook(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Search books", description = "Search books by title, author, or ISBN")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved books",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Book.class))))
    })
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntityModel<Book>>> searchBooks(@RequestParam Optional<String> title,
                                                               @RequestParam Optional<String> author,
                                                               @RequestParam Optional<String> isbn) {
        List<Book> books;

        if ((title.isEmpty() && isbn.isEmpty() && author.isEmpty()) || (title.isPresent() ? 1 : 0) + (author.isPresent() ? 1 : 0) + (isbn.isPresent() ? 1 : 0) != 1) {
            return ResponseEntity.badRequest().build();
        }

        if (title.isPresent()) {
            books = bookService.searchBooksByTitle(title.get());
        } else if (author.isPresent()) {
            books = bookService.searchBooksByAuthor(author.get());
        } else if (isbn.isPresent()) {
            books = bookService.searchBooksByIsbn(isbn.get());
        } else {
            return ResponseEntity.badRequest().build();
        }

        List<EntityModel<Book>> bookModels = books.stream()
                .map(this::toBookModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(bookModels);
    }

    private EntityModel<Book> toBookModel(Book book) {
        return EntityModel.of(book,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BookController.class).getBookById(book.getId())).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BookController.class).getAllBooks()).withRel("books"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BookController.class).updateBook(book.getId(), book)).withRel("update"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BookController.class).deleteBook(book.getId())).withRel("delete"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BookController.class).searchBooks(Optional.empty(), Optional.empty(), Optional.empty())).withRel("search"));
    }


}
