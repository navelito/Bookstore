package com.example.bookstore.dto;

import com.example.bookstore.model.Book;

public record BookResponseDto(String title, int price, int stock) {
    // Helper constructor to create a response from a Book
    public BookResponseDto(Book book, int stock) {
        this(book.getTitle(), book.getPrice(), stock);
    }
}
