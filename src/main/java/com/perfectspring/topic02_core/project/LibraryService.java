package com.perfectspring.topic02_core.project;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * The LIBRARIAN (business logic).
 *
 * Everything it needs arrives through the constructor:
 *   - BookRepository -> autowired by type (there's only one)
 *   - Notifier       -> TWO candidates, so we pick one by name with @Qualifier
 *   - maxBooks       -> read from library.properties with @Value
 */
@Service
public class LibraryService {

    private final BookRepository bookRepository;
    private final Notifier notifier;
    private final int maxBooksPerStudent;
    private final String libraryName;

    // How many books each student has borrowed.
    // (This data is about the LIBRARY as a whole, so storing it in a singleton is fine.)
    private final Map<String, Integer> borrowedCount = new HashMap<>();

    public LibraryService(BookRepository bookRepository,
                          @Qualifier("smsNotifier") Notifier notifier,       // TASK: remove @Qualifier and see what happens
                          @Value("${library.max-books-per-student}") int maxBooksPerStudent,
                          @Value("${library.name}") String libraryName) {
        this.bookRepository = bookRepository;
        this.notifier = notifier;
        this.maxBooksPerStudent = maxBooksPerStudent;
        this.libraryName = libraryName;
    }

    public void showCatalogue() {
        System.out.println("LIBRARY: " + libraryName + " (max " + maxBooksPerStudent + " books per student)");
        bookRepository.findAll().forEach(book -> System.out.println("   - " + book));
    }

    public void borrow(String student, String title) {
        int alreadyBorrowed = borrowedCount.getOrDefault(student, 0);
        if (alreadyBorrowed >= maxBooksPerStudent) {
            notifier.notify(student, "Sorry, you already have " + alreadyBorrowed + " books. Return one first.");
            return;
        }

        bookRepository.findByTitle(title).ifPresentOrElse(book -> {
            if (!book.isAvailable()) {
                notifier.notify(student, "\"" + title + "\" is already borrowed.");
                return;
            }
            book.setAvailable(false);
            borrowedCount.put(student, alreadyBorrowed + 1);
            notifier.notify(student, "You borrowed \"" + title + "\". Enjoy!");
        }, () -> notifier.notify(student, "We don't have \"" + title + "\"."));
    }

    public void giveBack(String student, String title) {
        bookRepository.findByTitle(title).ifPresent(book -> {
            book.setAvailable(true);
            borrowedCount.merge(student, -1, Integer::sum);
            notifier.notify(student, "Thanks for returning \"" + title + "\".");
        });
    }
}
