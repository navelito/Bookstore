package com.example.bookstore.dto;

public record OrderedBookDto(String title, int quantity, int pricePerBook, int subTotal) {
    // Helper constructor for the dto to calculate subtotal
    public OrderedBookDto(String title, int quantity, int pricePerBook) {
        this(title, quantity, pricePerBook, pricePerBook * quantity);
    }
}
