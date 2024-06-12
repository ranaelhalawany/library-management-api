package com.example.library.repository;

import com.example.library.model.Author;
import com.example.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book,Long> {
    List<Book> findByTitleContaining(String title);
    List<Book> findByAuthor_NameContaining(String authorName);
    List<Book> findByAuthor(Author author);
    List<Book> findByIsbnContaining(String isbn);
}
