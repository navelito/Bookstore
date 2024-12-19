package com.example.bookstore.dto;

import com.example.bookstore.model.Book;

// This class represents one item in an order
public record BookRequestDto(Book book, int quantity) {
}
