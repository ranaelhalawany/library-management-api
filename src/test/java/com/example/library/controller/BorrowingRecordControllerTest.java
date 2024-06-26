package com.example.library.controller;

import com.example.library.model.Book;
import com.example.library.model.BorrowingRecord;
import com.example.library.model.Customer;
import com.example.library.service.BorrowingRecordService;
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

@WebMvcTest(BorrowingRecordController.class)
public class BorrowingRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowingRecordService borrowingRecordService;

    @Test
    public void testGetAllBorrowingRecords_ValidRequest_Success() throws Exception {
        BorrowingRecord record1 = new BorrowingRecord(1L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(14));
        BorrowingRecord record2 = new BorrowingRecord(2L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(21));
        List<BorrowingRecord> records = Arrays.asList(record1, record2);
        Mockito.when(borrowingRecordService.getAllBorrowingRecords()).thenReturn(records);

        ResultActions result = mockMvc.perform(get("/api/v1/borrowings")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    public void testGetBorrowingRecordById_ExistingId_Success() throws Exception {
        BorrowingRecord borrowingRecord = new BorrowingRecord(1L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(14));
        Mockito.when(borrowingRecordService.getBorrowingRecordById(1L)).thenReturn(Optional.of(borrowingRecord));

        ResultActions result = mockMvc.perform(get("/api/v1/borrowings/1")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testGetBorrowingRecordById_NonExistingId_NotFound() throws Exception {
        Mockito.when(borrowingRecordService.getBorrowingRecordById(2L)).thenReturn(Optional.empty());

        ResultActions result = mockMvc.perform(get("/api/v1/borrowings/2")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testCreateBorrowingRecord_ValidRequest_Success() throws Exception {
        BorrowingRecord inputRecord = new BorrowingRecord(null, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(14));
        BorrowingRecord createdRecord = new BorrowingRecord(1L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(14));
        Mockito.when(borrowingRecordService.createBorrowingRecord(ArgumentMatchers.any(BorrowingRecord.class))).thenReturn(createdRecord);

        ResultActions result = mockMvc.perform(post("/api/v1/borrowings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"borrowDate\": \"" + LocalDate.now() + "\", \"returnDate\": \"" + LocalDate.now().plusDays(14) + "\"}"));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/hal+json")))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testUpdateBorrowingRecord_ExistingId_ValidRequest_Success() throws Exception {
        BorrowingRecord updatedRecord = new BorrowingRecord(1L, new Customer(), new Book(), LocalDate.now(), LocalDate.now().plusDays(21));
        Mockito.when(borrowingRecordService.updateBorrowingRecord(ArgumentMatchers.eq(1L), ArgumentMatchers.any(BorrowingRecord.class)))
                .thenReturn(Optional.of(updatedRecord));

        ResultActions result = mockMvc.perform(put("/api/v1/borrowings/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"borrowDate\": \"" + LocalDate.now() + "\", \"returnDate\": \"" + LocalDate.now().plusDays(21) + "\"}"));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.returnDate").value(LocalDate.now().plusDays(21).toString()));
    }

    @Test
    public void testUpdateBorrowingRecord_NonExistingId_NotFound() throws Exception {
        Mockito.when(borrowingRecordService.updateBorrowingRecord(ArgumentMatchers.eq(2L), ArgumentMatchers.any(BorrowingRecord.class)))
                .thenReturn(Optional.empty());

        ResultActions result = mockMvc.perform(put("/api/v1/borrowings/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"borrowDate\": \"" + LocalDate.now() + "\", \"returnDate\": \"" + LocalDate.now().plusDays(21) + "\"}"));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteBorrowingRecord_ExistingId_Success() throws Exception {
        Mockito.when(borrowingRecordService.deleteBorrowingRecord(1L)).thenReturn(true);

        ResultActions result = mockMvc.perform(delete("/api/v1/borrowings/1"));

        result.andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteBorrowingRecord_NonExistingId_NotFound() throws Exception {
        Mockito.when(borrowingRecordService.deleteBorrowingRecord(2L)).thenReturn(false);

        ResultActions result = mockMvc.perform(delete("/api/v1/borrowings/2"));

        result.andExpect(status().isNotFound());
    }

}
