package com.example.bookstore.service;

import com.example.bookstore.model.Book;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;


@Service
public class InventoryService {
    private final Map<Book, Integer> stock = new EnumMap<>(Book.class);

    public InventoryService() {
        // Initialize starting stock
        stock.put(Book.BOOK_A, 20);
        stock.put(Book.BOOK_B, 20);
        stock.put(Book.BOOK_C, 20);
        stock.put(Book.BOOK_D, 10); // Max 10 copies for Book D
    }

    public Map<Book, Integer> getStock() {
        return stock;
    }

    public void orderBook(Book book, int quantity) {
        int currentStock = stock.getOrDefault(book, 0);
        stock.put(book, currentStock - quantity);
    }

    public void restockBook(Book book, int quantity) {
        stock.put(book, stock.getOrDefault(book, 0) + quantity);
    }

    public boolean hasBookInStock(Book book, int quantity) {
        return stock.getOrDefault(book, 0) >= quantity;
    }
}
