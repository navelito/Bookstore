package com.example.bookstore.controller;

import com.example.bookstore.dto.*;
import com.example.bookstore.model.Book;
import com.example.bookstore.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class BookstoreController {

    @Autowired
    private InventoryService inventoryService;

    private static final int MAX_ORDER_VALUE = 120;
    private static final int MAX_RESTOCK_QUANTITY = 1000;


    @GetMapping("/books")
    public List<BookResponseDto> getBooks() {
        // Transform the Map<Book, Integer> into a List<BookResponseDto>
        List<BookResponseDto> dtoList = new ArrayList<>();

        for (Map.Entry<Book, Integer> entry : inventoryService.getStock().entrySet()) {
            // Create a BookResponseDto using the book's title, price, and current stock
            dtoList.add(new BookResponseDto(entry.getKey(), entry.getValue()));
        }

        return dtoList;
    }

    /*
     * Assumptions:
     * All books are initially in stock when the program starts.
     * Trying to order an empty cart will fail.
     * The entire order will be rejected if any book in the order is out of stock, even if other books are in stock.
     * The customer will be asked to try to place a new order without the out-of-stock books.
     * Spring Boot will reject the request with "400" if an unknown book, ex. (BOOK_F) is in the order.
     *
     */

    @PostMapping("/order")
    public ResponseEntity<?> orderBooks(@RequestBody List<BookRequestDto> orderItems) {

        // Get the total ordered books
        int totalBooksOrdered = orderItems.stream()
                .mapToInt(item -> item.quantity())
                .sum();
        // Fail if the quantity is 0
        if (totalBooksOrdered == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "No books placed in the order."));
        }

        // Get the total price of the ordered books
        int totalPrice = orderItems.stream()
                .mapToInt(item -> item.book().getPrice() * item.quantity())
                .sum();
        // Fail if the price is too high
        if (totalPrice > MAX_ORDER_VALUE) {
            return ResponseEntity.badRequest().body(Map.of("error", "Order of $" + totalPrice + " exceeds maximum allowed value of $" + MAX_ORDER_VALUE + "."));
        }

        // Verify we have the books in stock
        List<String> stockErrors = orderItems.stream()
                // Filter out books that are not in stock
                .filter(item -> !inventoryService.hasBookInStock(item.book(), item.quantity()))
                .map(item -> {
                    if (item.book() == Book.BOOK_D) {
                        return item.book().getTitle() + " is unfortunately sold out and no more copies exist in the world. " +
                                "Please remove " + item.book().getTitle() + " from your order and try again.";
                    } else {
                        return item.book().getTitle() + " is currently sold out. Please try to place the order again without this book.";
                    }
                })
                .toList();

        // Verify if there are any errors in order
        if (!stockErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", stockErrors));
        }

        // Setup a list of ordered books for the response
        List<OrderedBookDto> orderedBooks = new ArrayList<>();
        for (BookRequestDto item : orderItems) {
            // Call to inventory service to reduce the stock
            inventoryService.orderBook(item.book(), item.quantity());

            // Convert the bookRequestDto to an OrderedBookDto for the response
            // This is so we get a response containing the list of books and each book will have a title, quantity,
            // price per book and a sub-total (book price * quantity)
            orderedBooks.add(new OrderedBookDto(item.book().getTitle(), item.quantity(), item.book().getPrice()));
        }

        // Return a structured response with all ordered books and total price
        OrderResponseDto response = new OrderResponseDto(orderedBooks, totalPrice, "Order placed successfully!");
        return ResponseEntity.ok(response);
    }

    /*
     * Assumptions:
     * Allow restocking books: A, B, C in multiples of 10 (20, 30 40...), regardless of current stock level.
     * Book D never restocks as it's sold out worldwide.
     * If one restock item fails, the entire restock fails.
     * Made the assumption that you can restock even when stock isn't 0.
     * If the admin user only can restock at 0, then the code on line: XXX can be restored.
     * Due to "storage issues" there is a restock max limit of 1000 per book.
     */

    @PostMapping("/restock")
    public ResponseEntity<?> restockBook(@RequestBody List<BookRequestDto> restockItems) {
        // Setup a list of valid books the user is restocking
        List<BookRequestDto> validRestocks = new ArrayList<>();
        List<String> restockErrors = new ArrayList<>();

        for (BookRequestDto restockItem : restockItems) {
            Book book = restockItem.book();
            int quantity = restockItem.quantity();

            /*int currentStock = inventoryService.getStock().getOrDefault(book, 0);
            if (currentStock != 0) {
                restockErrors.add(("Cannot restock " + book.getTitle() + " unless stock is zero!"));
            }*/

            // Ensure restock quantity does not go over max limit of 1000 per book
            if (quantity > MAX_RESTOCK_QUANTITY) {
                restockErrors.add("Restock exceeds maximum allowed quantity of " + MAX_RESTOCK_QUANTITY + " for book " + book.getTitle() + ".");
            }
            // Ensure restock quantity is in multiples of 10
            else if (quantity % 10 != 0) {
                restockErrors.add("Restock quantity for " + book.getTitle() + " must be in multiples of 10.");
            }
            // Ensure the book isn't book D
            else if (book == Book.BOOK_D) {
                restockErrors.add("Cannot restock " + book.getTitle() +
                        ". as no more exists in the world. Please try to create a new restock order without " + book.getTitle());
            } else {
                validRestocks.add(restockItem);
            }
        }

        // Verify if there are any errors in restock order
        if (!restockErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("errors", restockErrors));
        }

        // Only restock the valid restock orders at the end
        for (BookRequestDto validRestock : validRestocks) {
            // Call to inventory service to increase the stock
            inventoryService.restockBook(validRestock.book(), validRestock.quantity());
        }

        // Setup a list of restocked books for the response
        List<RestockedBookDto> restockedItems = new ArrayList<>();
        for (BookRequestDto validRestock : validRestocks) {
            restockedItems.add(new RestockedBookDto(validRestock.book().getTitle(), validRestock.quantity()));
        }

        // Return a structured response with all restocked books
        RestockResponseDto response = new RestockResponseDto("Restocked successfully!", restockedItems);
        return ResponseEntity.ok(response);
    }
}
