package com.example.library.controller;

import com.example.library.model.BorrowingRecord;
import com.example.library.service.BorrowingRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/borrowings")
@RequiredArgsConstructor
@Tag(name = "Borrowing Record Controller", description = "API for managing borrowing records")

public class BorrowingRecordController {

    private final BorrowingRecordService borrowingRecordService;

    @Operation(summary = "Get all borrowing records", description = "Retrieve a list of all borrowing records")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BorrowingRecord.class))))
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EntityModel<BorrowingRecord>>> getAllBorrowingRecords() {
        List<BorrowingRecord> borrowingRecords = borrowingRecordService.getAllBorrowingRecords();
        List<EntityModel<BorrowingRecord>> borrowingRecordModels = borrowingRecords.stream()
                .map(this::toBorrowingRecordModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(borrowingRecordModels);
    }

    @Operation(summary = "Get borrowing record by ID", description = "Retrieve a specific borrowing record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved borrowing record",
                    content = @Content(schema = @Schema(implementation = BorrowingRecord.class))),
            @ApiResponse(responseCode = "404", description = "Borrowing record not found")
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<BorrowingRecord>> getBorrowingRecordById(@PathVariable Long id) {
        Optional<BorrowingRecord> borrowingRecord = borrowingRecordService.getBorrowingRecordById(id);

        return borrowingRecord.map(value -> ResponseEntity.ok(toBorrowingRecordModel(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create a new borrowing record", description = "Create a new borrowing record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created borrowing record",
                    content = @Content(schema = @Schema(implementation = BorrowingRecord.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<EntityModel<BorrowingRecord>> createBorrowingRecord(@RequestBody BorrowingRecord borrowingRecord) {
        BorrowingRecord createdBorrowingRecord = borrowingRecordService.createBorrowingRecord(borrowingRecord);
        return ResponseEntity.ok(toBorrowingRecordModel(createdBorrowingRecord));
    }

    @Operation(summary = "Update an existing borrowing record", description = "Update an existing borrowing record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated borrowing record",
                    content = @Content(schema = @Schema(implementation = BorrowingRecord.class))),
            @ApiResponse(responseCode = "404", description = "Borrowing record not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<BorrowingRecord>> updateBorrowingRecord(@PathVariable Long id, @RequestBody BorrowingRecord updatedBorrowingRecord) {
        Optional<BorrowingRecord> borrowingRecord = borrowingRecordService.updateBorrowingRecord(id, updatedBorrowingRecord);

        return borrowingRecord.map(value -> {
            EntityModel<BorrowingRecord> borrowingRecordModel = toBorrowingRecordModel(value);
            return ResponseEntity.ok(borrowingRecordModel);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete a borrowing record", description = "Delete a borrowing record by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted borrowing record"),
            @ApiResponse(responseCode = "404", description = "Borrowing record not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrowingRecord(@PathVariable Long id) {
        if (borrowingRecordService.deleteBorrowingRecord(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Search borrowing records", description = "Search for borrowing records by user ID or book ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved borrowing records",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BorrowingRecord.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchBorrowingRecords(
            @RequestParam Optional<Long> userId,
            @RequestParam Optional<Long> bookId) {
        List<BorrowingRecord> borrowingRecords;
        if ((userId.isEmpty() && bookId.isEmpty()) || (userId.isPresent() && bookId.isPresent())) {
            return ResponseEntity.badRequest().build();
        }

        if (userId.isPresent()) {
            borrowingRecords = borrowingRecordService.findBorrowingRecordsByUserId(userId.get());
        } else if (bookId.isPresent()) {
            borrowingRecords = borrowingRecordService.findBorrowingRecordsByBookId(bookId.get());
        } else {
            return ResponseEntity.badRequest().build();
        }
        List<EntityModel<BorrowingRecord>> borrowingRecordModels = borrowingRecords.stream()
                .map(this::toBorrowingRecordModel)
                .collect(Collectors.toList());
        return ResponseEntity.ok(borrowingRecordModels);
    }

    private EntityModel<BorrowingRecord> toBorrowingRecordModel(BorrowingRecord borrowingRecord) {
        return EntityModel.of(borrowingRecord,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BorrowingRecordController.class).getBorrowingRecordById(borrowingRecord.getId())).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BorrowingRecordController.class).getAllBorrowingRecords()).withRel("all"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BorrowingRecordController.class).updateBorrowingRecord(borrowingRecord.getId(), borrowingRecord)).withRel("update"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BorrowingRecordController.class).deleteBorrowingRecord(borrowingRecord.getId())).withRel("delete"),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(BorrowingRecordController.class).searchBorrowingRecords(Optional.empty(), Optional.empty())).withRel("search"));
    }

}
