package com.example.library.controller;

import com.example.library.model.Customer;
import com.example.library.service.CustomerService;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    public void testGetAllCustomers_ValidRequest_Success() throws Exception {
        Customer customer1 = new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123");
        Customer customer2 = new Customer(2L, "Jane Smith", "jane@example.com", "456 Elm St", "0112345678", "password456");
        List<Customer> customers = Arrays.asList(customer1, customer2);
        Mockito.when(customerService.getAllCustomers()).thenReturn(customers);

        ResultActions result = mockMvc.perform(get("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[1].name").value("Jane Smith"));
    }

    @Test
    public void testGetCustomerById_ExistingId_Success() throws Exception {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123");
        Mockito.when(customerService.getCustomerById(1L)).thenReturn(Optional.of(customer));

        ResultActions result = mockMvc.perform(get("/api/v1/customers/1")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testGetCustomerById_NonExistingId_NotFound() throws Exception {
        Mockito.when(customerService.getCustomerById(2L)).thenReturn(Optional.empty());

        ResultActions result = mockMvc.perform(get("/api/v1/customers/2")
                .contentType(MediaType.APPLICATION_JSON));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testCreateCustomer_ValidCustomer_Success() throws Exception {
        Customer inputCustomer = new Customer(null, "New Customer", "new@example.com", "789 Oak St", "01115000153", "newpassword");
        Customer createdCustomer = new Customer(1L, "New Customer", "new@example.com", "789 Oak St", "01115000153", "newpassword");
        Mockito.when(customerService.createCustomer(ArgumentMatchers.any(Customer.class))).thenReturn(createdCustomer);

        ResultActions result = mockMvc.perform(post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"New Customer\", \"email\": \"new@example.com\", \"address\": \"789 Oak St\", \"phoneNumber\": \"01115000153\", \"password\": \"newpassword\"}"));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Customer"));
    }

    @Test
    public void testUpdateCustomer_ExistingId_ValidCustomer_Success() throws Exception {
        Customer updatedCustomer = new Customer(1L, "Updated Customer", "updated@example.com", "789 Oak St", "01115000153", "updatedpassword");
        Mockito.when(customerService.updateCustomer(ArgumentMatchers.eq(1L), ArgumentMatchers.any(Customer.class)))
                .thenReturn(Optional.of(updatedCustomer));

        ResultActions result = mockMvc.perform(put("/api/v1/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Customer\", \"email\": \"updated@example.com\", \"address\": \"789 Oak St\", \"phoneNumber\": \"01115000153\", \"password\": \"updatedpassword\"}"));

        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Updated Customer"));
    }

    @Test
    public void testUpdateCustomer_NonExistingId_NotFound() throws Exception {
        Mockito.when(customerService.updateCustomer(ArgumentMatchers.eq(2L), ArgumentMatchers.any(Customer.class)))
                .thenReturn(Optional.empty());

        ResultActions result = mockMvc.perform(put("/api/v1/customers/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"Updated Customer\", \"email\": \"updated@example.com\", \"address\": \"789 Oak St\", \"phoneNumber\": \"01115000153\", \"password\": \"updatedpassword\"}"));

        result.andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteCustomer_ExistingId_Success() throws Exception {
        Mockito.when(customerService.deleteCustomer(1L)).thenReturn(true);

        ResultActions result = mockMvc.perform(delete("/api/v1/customers/1"));

        result.andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteCustomer_NonExistingId_NotFound() throws Exception {
        Mockito.when(customerService.deleteCustomer(2L)).thenReturn(false);

        ResultActions result = mockMvc.perform(delete("/api/v1/customers/2"));

        result.andExpect(status().isNotFound());
    }

    private EntityModel<Customer> toCustomerModel(Customer customer) {

        return EntityModel.of(customer);
    }
}
