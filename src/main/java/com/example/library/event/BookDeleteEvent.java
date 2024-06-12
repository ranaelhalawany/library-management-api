package com.example.library.event;

import com.example.library.model.Book;
import org.springframework.context.ApplicationEvent;

public class BookDeleteEvent extends ApplicationEvent {

    private final Book book;

    public BookDeleteEvent(Object source, Book book) {
        super(source);
        this.book = book;
    }

    public Book getBook() {
        return book;
    }
}
