package com.example.library.event;

import com.example.library.model.Customer;
import com.example.library.repository.BorrowingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomerDeleteListener {

    private final BorrowingRecordRepository borrowingRecordRepository;

    @EventListener
    @Transactional
    public void handleCustomerDeleteEvent(CustomerDeleteEvent event) {
        Customer deletedCustomer = event.getCustomer();
        borrowingRecordRepository.deleteByCustomer(deletedCustomer);
    }
}
