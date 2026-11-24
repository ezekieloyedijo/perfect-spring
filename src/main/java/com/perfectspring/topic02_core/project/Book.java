package com.perfectspring.topic02_core.project;

/** A plain Java class (NOT a bean). Just data about one book. */
public class Book {

    private final String title;
    private final String author;
    private boolean available = true;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return "\"" + title + "\" by " + author + (available ? "" : "  [BORROWED]");
    }
}
