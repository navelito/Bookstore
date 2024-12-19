package com.example.bookstore.controller;

import com.example.bookstore.model.Book;
import com.example.bookstore.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest // Loads the whole application context
@AutoConfigureMockMvc
public class BookstoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InventoryService inventoryService;

    @BeforeEach
    void resetStock() {
        // Reset stock before each test to ensure they won't
        inventoryService.getStock().put(Book.BOOK_A, 20);
        inventoryService.getStock().put(Book.BOOK_B, 20);
        inventoryService.getStock().put(Book.BOOK_C, 20);
        inventoryService.getStock().put(Book.BOOK_D, 10);
    }


    @Test
    public void testInitialBooks() throws Exception {
        // Test the /books endpoint
        mockMvc.perform(get("/api/books"))
                .andDo(print())
                // Ensure it was successful
                .andExpect(status().isOk())

                // Ensure we have 4 books
                .andExpect(jsonPath("$.length()").value(4))

                // Validate the books
                .andExpect(jsonPath("$[0].title").value("Fellowship of the book"))
                .andExpect(jsonPath("$[0].price").value(5))
                .andExpect(jsonPath("$[0].stock").value(20))

                .andExpect(jsonPath("$[1].title").value("Books and the chamber of books"))
                .andExpect(jsonPath("$[1].price").value(10))
                .andExpect(jsonPath("$[1].stock").value(20))

                .andExpect(jsonPath("$[2].title").value("The Return of the Book"))
                .andExpect(jsonPath("$[2].price").value(15))
                .andExpect(jsonPath("$[2].stock").value(20))

                .andExpect(jsonPath("$[3].title").value("Limited Collectors Edition"))
                .andExpect(jsonPath("$[3].price").value(75))
                .andExpect(jsonPath("$[3].stock").value(10));
    }

    @Test
    public void testOrderBooksWorks() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_A","quantity":5},
                  {"book":"BOOK_B","quantity":5}
                ]
                """;

        // The expected total price of the books in the order (75)
        int expectedTotalPrice = Book.BOOK_A.getPrice() * 5 + Book.BOOK_B.getPrice() * 5;

        // Test the orders endpoint
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                // Ensure the response contains 2 books, a message and the total price
                .andExpect(jsonPath("$.orderedBooks", hasSize(2)))
                .andExpect(jsonPath("$.message").value("Order placed successfully!"))
                .andExpect(jsonPath("$.totalPrice").value(expectedTotalPrice));

        // Verify that they stock was reduced
        assertEquals(15, inventoryService.getStock().get(Book.BOOK_A));
        assertEquals(15, inventoryService.getStock().get(Book.BOOK_B));
    }

    @Test
    public void testOrderBooksWithEmptyOrder() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_A","quantity":0},
                  {"book":"BOOK_B","quantity":0},
                  {"book":"BOOK_C","quantity":0},
                  {"book":"BOOK_D","quantity":0}
                ]
                """;

        // Attempt to order empty cart
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No books placed in the order."));
    }

    @Test
    public void testOrderBooksTooExpensiveOrder() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_D","quantity":5}
                ]
                """;

        // Test ordering a too expensive order
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(" exceeds maximum allowed value of $120.")));
    }

    @Test
    public void testOrderBooksNotInStock() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_A","quantity":23}
                ]
                """;

        // Test ordering more books than available in stock
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Fellowship of the book is currently sold out. Please try to place the order again without this book."));
    }

    @Test
    public void testOrderBookDOutOfStock() throws Exception {
        // Set Book D's stock to 0 to simulate it being sold out
        inventoryService.getStock().put(Book.BOOK_D, 0);

        String requestJson = """
                [
                  {"book":"BOOK_D","quantity":1}
                ]
                """;

        // Test ordering Book D
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Limited Collectors Edition is unfortunately sold out and no more copies exist in the world. " +
                        "Please remove Limited Collectors Edition from your order and try again."));

        // Verify that the stock for book D didn't change
        assertEquals(0, inventoryService.getStock().get(Book.BOOK_D));

    }


    // Test the endpoints don't allow using an unknown book, springboot should throw json parse error
    @Test
    public void testOrderBooksUnknownBook() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_UNKNOWN","quantity":1}
                ]
                """;

        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRestockBook_BookD() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_D","quantity":10}
                ]
                """;

        // Attempt to restock Book D
        mockMvc.perform(post("/api/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(httpBasic("Uncle_Bob_1337", "TomCruiseIsUnder170cm"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").value("Cannot restock Limited Collectors Edition. as no more exists in the world. " +
                        "Please try to create a new restock order without Limited Collectors Edition"));

        // Ensure that the book D stock wasn't changed
        int bookDStock = inventoryService.getStock().getOrDefault(Book.BOOK_D, 0);
        assertEquals(bookDStock, 10);
    }

    @Test
    void testRestockBookSuccessful() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_A","quantity":10},
                  {"book":"BOOK_B","quantity":10},
                  {"book":"BOOK_C","quantity":10}
                ]
                """;

        // Attempt to restock Book A, B and C
        mockMvc.perform(post("/api/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(httpBasic("Uncle_Bob_1337", "TomCruiseIsUnder170cm"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Restocked successfully!"));

        // Verify that the stock was updated correctly
        assertEquals(30, inventoryService.getStock().get(Book.BOOK_A));
        assertEquals(30, inventoryService.getStock().get(Book.BOOK_B));
        assertEquals(30, inventoryService.getStock().get(Book.BOOK_C));
    }

    @Test
    void testRestockBookInvalidRestockAmount() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_A","quantity":15},
                  {"book":"BOOK_B","quantity":7},
                  {"book":"BOOK_C","quantity":23}
                ]
                """;

        // Attempt to restock Book A, B and C not multiple of 10
        mockMvc.perform(post("/api/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(httpBasic("Uncle_Bob_1337", "TomCruiseIsUnder170cm"))
                )
                .andExpect(jsonPath("$.errors", hasSize(3)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Restock quantity for Fellowship of the book must be in multiples of 10."))
                .andExpect(jsonPath("$.errors[1]").value("Restock quantity for Books and the chamber of books must be in multiples of 10."))
                .andExpect(jsonPath("$.errors[2]").value("Restock quantity for The Return of the Book must be in multiples of 10."));

        // Verify that the stock didn't change
        assertEquals(20, inventoryService.getStock().get(Book.BOOK_A));
        assertEquals(20, inventoryService.getStock().get(Book.BOOK_B));
        assertEquals(20, inventoryService.getStock().get(Book.BOOK_C));

    }

    @Test
    void testRestockBookExceedsMaxLimit() throws Exception {
        String requestJson = """
                [
                  {"book":"BOOK_A","quantity":1005},
                  {"book":"BOOK_B","quantity":2335},
                  {"book":"BOOK_C","quantity":5455}
                ]
                """;

        // Attempt to restock Book A, B and C over allowed quantity limit
        mockMvc.perform(post("/api/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson)
                        .with(httpBasic("Uncle_Bob_1337", "TomCruiseIsUnder170cm"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("Restock exceeds maximum allowed quantity of 1000 for book Fellowship of the book."))
                .andExpect(jsonPath("$.errors[1]").value("Restock exceeds maximum allowed quantity of 1000 for book Books and the chamber of books."))
                .andExpect(jsonPath("$.errors[2]").value("Restock exceeds maximum allowed quantity of 1000 for book The Return of the Book."));

        // Verify that the stock didn't change
        assertEquals(20, inventoryService.getStock().get(Book.BOOK_A));
        assertEquals(20, inventoryService.getStock().get(Book.BOOK_B));
        assertEquals(20, inventoryService.getStock().get(Book.BOOK_C));

    }
}
