package com.example.bookstore.model;

// Enum holding all the available books (name and price)
public enum Book {
    BOOK_A("Fellowship of the book", 5),
    BOOK_B("Books and the chamber of books", 10),
    BOOK_C("The Return of the Book", 15),
    BOOK_D("Limited Collectors Edition", 75);

    private final String title;
    private final int price;

    Book(String title, int price) {
        this.title = title;
        this.price = price;
    }

    public String getTitle() {
        return title;
    }

    public int getPrice() {
        return price;
    }
}
