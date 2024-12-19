package com.example.bookstore.dto;

import java.util.List;

// This class represents response of ordered books and total price
public record OrderResponseDto(List<OrderedBookDto> orderedBooks, int totalPrice, String message) {
}
