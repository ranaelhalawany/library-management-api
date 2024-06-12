package com.example.library.controller;

import com.example.library.model.Author;
import com.example.library.service.AuthorService;
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
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "Author Controller", description = "API for managing authors")
public class AuthorController {

    private final AuthorService authorService;

    @Operation(summary = "Get all authors", description = "Retrieve a list of all authors")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Author.class))))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntityModel<Author>>> getAllAuthors() {
        List<Author> authors = authorService.getAllAuthors();
        List<EntityModel<Author>> authorModels = authors.stream()
                .map(this::toAuthorModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(authorModels);
    }


    @Operation(summary = "Get author by ID", description = "Retrieve a specific author by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved author",
                    content = @Content(schema = @Schema(implementation = Author.class))),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Author>> getAuthorById(@PathVariable Long id) {
        Optional<Author> author = authorService.getAuthorById(id);

        return author.map(value -> ResponseEntity.ok(toAuthorModel(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @Operation(summary = "Create a new author", description = "Create a new author")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created author",
                    content = @Content(schema = @Schema(implementation = Author.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Author>> createAuthor(@Valid @RequestBody Author author) {
        Author createdAuthor = authorService.createAuthor(author);
        return ResponseEntity.created(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AuthorController.class)
                        .getAuthorById(createdAuthor.getId())).toUri())
                .body(toAuthorModel(createdAuthor));
    }

    @Operation(summary = "Update an existing author", description = "Update an existing author by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated author",
                    content = @Content(schema = @Schema(implementation = Author.class))),
            @ApiResponse(responseCode = "404", description = "Author not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<Author>> updateAuthor(@PathVariable Long id, @Valid @RequestBody Author updatedAuthor) {
        Optional<Author> author = authorService.updateAuthor(id, updatedAuthor);

        return author.map(value -> {
            EntityModel<Author> authorModel = toAuthorModel(value);
            return ResponseEntity.ok(authorModel);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an author", description = "Delete an author by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted author"),
            @ApiResponse(responseCode = "404", description = "Author not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable Long id) {
        if (authorService.deleteAuthor(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private EntityModel<Author> toAuthorModel(Author author) {
        return EntityModel.of(author,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AuthorController.class).getAuthorById(author.getId())).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AuthorController.class).getAllAuthors()).withRel("authors"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AuthorController.class).updateAuthor(author.getId(), author)).withRel("update"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AuthorController.class).deleteAuthor(author.getId())).withRel("delete"));


    }


}
