package com.example.library.event;

import com.example.library.model.Author;
import org.springframework.context.ApplicationEvent;

public class AuthorDeleteEvent extends ApplicationEvent {

    private final Author author;

    public AuthorDeleteEvent(Object source, Author author) {
        super(source);
        this.author = author;
    }

    public Author getAuthor() {
        return author;
    }
}
