package com.example.library.service;

import com.example.library.event.BookDeleteEvent;
import com.example.library.model.Author;
import com.example.library.model.Book;
import com.example.library.repository.AuthorRepository;
import com.example.library.repository.BookRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final ApplicationEventPublisher eventPublisher;


    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional
    public Book createBook(Book book) {
        Author author = book.getAuthor();

        if (author != null) {
            Optional<Author> existingAuthor = authorRepository.findByName(author.getName());

            if (existingAuthor.isPresent()) {
                book.setAuthor(existingAuthor.get());
            } else {
                author = authorRepository.save(author);
                book.setAuthor(author);
            }
        }

        return bookRepository.save(book);
    }

    public Optional<Book> updateBook(Long id, Book updatedBook) {
        return bookRepository.findById(id)
                .map(existingBook -> {
                    existingBook.setTitle(updatedBook.getTitle());
                    existingBook.setAuthor(updatedBook.getAuthor());
                    existingBook.setIsbn(updatedBook.getIsbn());
                    existingBook.setPublicationDate(updatedBook.getPublicationDate());
                    return bookRepository.save(existingBook);
                });
    }

    public boolean deleteBook(Long id) {
        return bookRepository.findById(id)
                .map(book -> {
                    if (!book.isAvailable()) {
                        throw new IllegalArgumentException("The book is currently borrowed and cannot be deleted.");
                    }
                    eventPublisher.publishEvent(new BookDeleteEvent(this, book));
                    bookRepository.delete(book);
                    return true;
                })
                .orElse(false);
    }


    public List<Book> searchBooksByTitle(String title) {
        return bookRepository.findByTitleContaining(title);
    }

    public List<Book> searchBooksByAuthor(String authorName) {
        return bookRepository.findByAuthor_NameContaining(authorName);
    }

    public List<Book> searchBooksByIsbn(String isbn) {
        return bookRepository.findByIsbnContaining(isbn);
    }

}
