package com.example.library.service;

import com.example.library.event.AuthorDeleteEvent;
import com.example.library.model.Author;
import com.example.library.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorService {


    private final AuthorRepository authorRepository;
    private final ApplicationEventPublisher eventPublisher;


    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    public Optional<Author> getAuthorById(Long id) {
        return authorRepository.findById(id);
    }

    public Author createAuthor(Author author) {


        return authorRepository.save(author);
    }

    public Optional<Author> updateAuthor(Long id, Author updatedAuthor) {
        return authorRepository.findById(id).map(author -> {
            author.setName(updatedAuthor.getName());
            author.setBirthDate(updatedAuthor.getBirthDate());
            author.setNationality(updatedAuthor.getNationality());
            return authorRepository.save(author);
        });
    }

    public boolean deleteAuthor(Long id) {

        return authorRepository.findById(id).map(author -> {
            eventPublisher.publishEvent(new AuthorDeleteEvent(this, author));

            authorRepository.delete(author);
            return true;
        }).orElse(false);
    }

    LocalDate parseAndEditBirthdate(String birthdateStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate birthdate = LocalDate.parse(birthdateStr, formatter);

            birthdate = birthdate.plusDays(1);

            return birthdate;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format, expected yyyy-MM-dd");
        }
    }
}
