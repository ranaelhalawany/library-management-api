package com.example.library.service;

import com.example.library.event.CustomerDeleteEvent;
import com.example.library.model.Customer;
import com.example.library.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        customers.add(new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123"));
        customers.add(new Customer(2L, "Jane Smith", "jane@example.com", "456 Elm St", "0112345678", "password456"));
        when(customerRepository.findAll()).thenReturn(customers);

        List<Customer> result = customerService.getAllCustomers();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("jane@example.com", result.get(1).getEmail());
    }

    @Test
    public void testGetCustomerById_ExistingId() {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.getCustomerById(1L);

        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
    }

    @Test
    public void testGetCustomerById_NonExistingId() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerById(2L);

        assertFalse(result.isPresent());
    }

    @Test
    public void testCreateCustomer_ValidCustomer() {
        Customer inputCustomer = new Customer(null, "New Customer", "new@example.com", "789 Oak St", "0198765432", "newpassword");
        Customer savedCustomer = new Customer(1L, "New Customer", "new@example.com", "789 Oak St", "0198765432", "newpassword");
        when(customerRepository.save(inputCustomer)).thenReturn(savedCustomer);

        Customer result = customerService.createCustomer(inputCustomer);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    public void testCreateCustomer_DuplicateEmail() {
        Customer inputCustomer = new Customer(null, "New Customer", "duplicate@example.com", "789 Oak St", "0198765432", "newpassword");
        when(customerRepository.save(inputCustomer)).thenThrow(DataIntegrityViolationException.class);

        assertThrows(IllegalArgumentException.class, () -> customerService.createCustomer(inputCustomer));
    }

    @Test
    public void testUpdateCustomer_ExistingId_ValidCustomer() {
        Customer updatedCustomer = new Customer(1L, "Updated Customer", "updated@example.com", "789 Oak St", "0198765432", "updatedpassword");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(updatedCustomer));
        when(customerRepository.save(updatedCustomer)).thenReturn(updatedCustomer);

        Optional<Customer> result = customerService.updateCustomer(1L, updatedCustomer);

        assertTrue(result.isPresent());
        assertEquals("Updated Customer", result.get().getName());
        assertEquals("updated@example.com", result.get().getEmail());
    }

    @Test
    public void testUpdateCustomer_NonExistingId() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.updateCustomer(2L, new Customer());

        assertFalse(result.isPresent());
    }

    @Test
    public void testDeleteCustomer_ExistingId() {
        Customer customer = new Customer(1L, "John Doe", "john@example.com", "123 Main St", "0123456789", "password123");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        boolean result = customerService.deleteCustomer(1L);

        assertTrue(result);
        verify(eventPublisher, times(1)).publishEvent(any(CustomerDeleteEvent.class));
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    public void testDeleteCustomer_NonExistingId() {
        when(customerRepository.findById(2L)).thenReturn(Optional.empty());

        boolean result = customerService.deleteCustomer(2L);

        assertFalse(result);
        verify(eventPublisher, never()).publishEvent(any(CustomerDeleteEvent.class));
        verify(customerRepository, never()).delete(any(Customer.class));
    }
}
