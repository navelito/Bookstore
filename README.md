## Technologies Used
- **Java 21**
- **Spring Boot 3.4.0**
- **Maven**

## API Endpoints

1. **Get All Books**
   - **URL**: `/api/books`
   - **Method**: `GET`
   - **Description**: Gets a list of all available books.

2. **Place an Order**
   - **URL**: `/api/order`
   - **Method**: `POST`
   - **Description**: Allows customers to place an order for books.

3. **Restock Books**
   - **URL**: `/api/restock`
   - **Method**: `POST`
   - **Description**: Allows the admin to restock books.


## Assumptions:
- All books are initially in stock when the program starts.
- Trying to order an empty cart will fail.
- The entire order will be rejected if any book in the order is out of stock, even if other books are in stock.
- The customer will be asked to try to place a new order without the out-of-stock books.
- Spring Boot will reject the request with "400" if an unknown book, ex. (BOOK_F) is in the order.
- Allow restocking books: A, B, C in multiples of 10 (20, 30 40...), regardless of current stock level.
- Book D never restocks as it's sold out worldwide.
- If one restock item fails, the entire restock fails.
- Made the assumption that you can restock even when stock isn't 0.
- If the admin user only can restock at 0, then the code on line: 126 can be restored.
- Due to "storage issues" there is a restock max limit of 1000 per book.
     
