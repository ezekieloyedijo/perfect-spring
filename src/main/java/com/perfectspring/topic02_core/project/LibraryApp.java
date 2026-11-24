package com.perfectspring.topic02_core.project;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * PROJECT 2 — School Library
 *
 * Watch the output and spot each Spring Core idea:
 *   - @PostConstruct message when the shelves are stocked
 *   - which Notifier was injected (SMS, because of @Qualifier)
 *   - the borrowing limit coming from library.properties
 */
public class LibraryApp {

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(LibraryConfig.class)) {

            LibraryService library = context.getBean(LibraryService.class);

            library.showCatalogue();
            System.out.println();

            library.borrow("Ada", "Things Fall Apart");
            library.borrow("Ada", "Clean Code");
            library.borrow("Ada", "Head First Java");      // over the limit (2)
            library.borrow("Tunde", "Clean Code");         // already borrowed by Ada
            library.giveBack("Ada", "Clean Code");
            library.borrow("Tunde", "Clean Code");         // now it works
            library.borrow("Tunde", "Harry Potter");       // not in the library

            System.out.println();
            library.showCatalogue();
        }
    }
}
