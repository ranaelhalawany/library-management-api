package com.example.library.event;

import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthorDeleteEventListener {

    private final BookRepository bookRepository;

    @EventListener
    @Transactional
    public void handleAuthorDeleteEvent(AuthorDeleteEvent event) {
        Author deletedAuthor = event.getAuthor();
        List<Book> books = bookRepository.findByAuthor(deletedAuthor);
        for (Book book : books) {
            book.setAuthor(null);
            bookRepository.save(book);
        }
    }
}
