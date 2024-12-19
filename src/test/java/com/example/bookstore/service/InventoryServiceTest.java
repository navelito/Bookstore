package com.example.bookstore.service;

import com.example.bookstore.model.Book;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryServiceTest {
    @Test
    public void testInventoryServiceHasBookInStock() {
        InventoryService inventoryService = new InventoryService();

        assertTrue(inventoryService.hasBookInStock(Book.BOOK_A, 1));
        assertTrue(inventoryService.hasBookInStock(Book.BOOK_B, 1));
        assertTrue(inventoryService.hasBookInStock(Book.BOOK_C, 1));
        assertTrue(inventoryService.hasBookInStock(Book.BOOK_D, 1));
    }

    @Test
    public void testInventoryServiceRestock() {
        // Initialize inventory with 0 of BOOK_A
        InventoryService inventoryService = new InventoryService();
        inventoryService.getStock().put(Book.BOOK_A, 0);
        assertFalse(inventoryService.hasBookInStock(Book.BOOK_A, 10));

        // Restock 10 of BOOK_A and assert that it was restocked
        inventoryService.restockBook(Book.BOOK_A, 10);
        assertTrue(inventoryService.hasBookInStock(Book.BOOK_A, 10));

        // Order 10 of BOOK_A and ensure it was removed
        inventoryService.orderBook(Book.BOOK_A, 10);
        assertFalse(inventoryService.hasBookInStock(Book.BOOK_A, 10));
    }
}
