package com.example.bookstore.dto;

import java.util.List;

// This class represents response of restocked books
public record RestockResponseDto(String message, List<RestockedBookDto> restockedItems) {
}
