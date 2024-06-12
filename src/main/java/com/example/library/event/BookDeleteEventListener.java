package com.example.library.event;

import com.example.library.model.Book;
import com.example.library.repository.BorrowingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BookDeleteEventListener {

    private final BorrowingRecordRepository borrowingRecordRepository;

    @EventListener
    @Transactional
    public void handleBookDeleteEvent(BookDeleteEvent event) {
        Book deletedBook = event.getBook();
        borrowingRecordRepository.deleteByBook(deletedBook);
    }
}
