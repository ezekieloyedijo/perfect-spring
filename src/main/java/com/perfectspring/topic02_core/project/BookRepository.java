package com.perfectspring.topic02_core.project;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The RECORDS OFFICER: keeps the list of books.
 *
 * @Repository     -> badge: "I handle data". Spring finds me while scanning.
 * singleton       -> one shared bookshelf for the whole app (the default scope).
 * @PostConstruct  -> fill the shelves when the library opens.
 *
 * (In Topic 3 this list becomes a real database table.)
 */
@Repository
public class BookRepository {

    private final List<Book> shelf = new ArrayList<>();

    @PostConstruct
    void stockTheShelves() {
        shelf.add(new Book("Things Fall Apart", "Chinua Achebe"));
        shelf.add(new Book("Purple Hibiscus", "Chimamanda Ngozi Adichie"));
        shelf.add(new Book("Clean Code", "Robert C. Martin"));
        shelf.add(new Book("Head First Java", "Kathy Sierra"));
        System.out.println("[BookRepository] @PostConstruct: " + shelf.size() + " books placed on the shelf.");
    }

    public List<Book> findAll() {
        return shelf;
    }

    public Optional<Book> findByTitle(String title) {
        return shelf.stream().filter(b -> b.getTitle().equalsIgnoreCase(title)).findFirst();
    }
}
