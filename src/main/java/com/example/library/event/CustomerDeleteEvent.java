package com.example.library.event;

import com.example.library.model.Customer;
import org.springframework.context.ApplicationEvent;

public class CustomerDeleteEvent extends ApplicationEvent {

    private final Customer customer;

    public CustomerDeleteEvent(Object source, Customer customer) {
        super(source);
        this.customer = customer;
    }

    public Customer getCustomer() {
        return customer;
    }
}
