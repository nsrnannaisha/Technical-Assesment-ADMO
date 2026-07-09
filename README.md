# Technical Test – Nisrina Annaisha Sarnadi
### Backend Engineer Intern at PT Astra Digital Mobil

# Order Service
A backend service for a simple e-commerce shop to manage orders.

## Versions

| Component | Version                       |
|---|-------------------------------|
| Java | 21 (LTS)                      |
| Spring Boot | 4.1.0                         |
| Build tool | Gradle (Kotlin DSL)           |
| Database | H2 (in-memory, JPA/Hibernate) |

## Build, run, and test

### Prerequisites
- JDK 21
- No local database required — H2 runs in-memory and is bundled as a dependency.

### Build
```bash
./gradlew build
```

### Run
```bash
./gradlew bootRun
```
The service starts on `http://localhost:8080`.

### Test
```bash
./gradlew test
```
This runs the full suite in a single command. All tests use H2 in-memory,  no external setup is needed.

## Exercising the API

Base URL: `http://localhost:8080/orders`

### Create an order
```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Andi Wijaya",
    "items": [
      { "productName": "Apple", "quantity": 3, "unitPrice": 5000 },
      { "productName": "Bread Loaf", "quantity": 1, "unitPrice": 2000 }
    ]
  }'
```
Returns `201 Created` with a server-assigned `orderId`, `status: "CREATED"`, and a computed `totalAmount` of `17000`.

### Read Order
#### Single order
```bash
curl http://localhost:8080/orders/{id}
```
Returns `404 Not Found` with a structured error body if the id doesn't exist.

#### Multiple orders (paginated, sortable)
```bash
curl "http://localhost:8080/orders?page=0&size=20&sort=newest"
```
Supported `sort` values:
- `newest`: most recently created first
- `highest_total`: highest `totalAmount` first
- `oldest_unpaid`: orders still in `CREATED` status, oldest first, then the rest

An unknown `sort` value returns `400 Bad Request` (`INVALID_SORT_KEY`).

### Update an order
```bash
curl -X PUT http://localhost:8080/orders/{id} \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Andi W.",
    "items": [
      { "productName": "Apple", "quantity": 5, "unitPrice": 5000 }
    ]
  }'
```
- `404` if the order doesn't exist.
- `409 Conflict` (`ITEMS_IMMUTABLE`) if the order is already `PAID` or later and the `items` list is changed. `customerName` may still be updated at any stage.

### Change order status
```bash
curl -X PATCH http://localhost:8080/orders/{id}/status \
  -H "Content-Type: application/json" \
  -d '{ "status": "PAID" }'
```
```bash
curl -X PATCH http://localhost:8080/orders/{id}/status \
  -H "Content-Type: application/json" \
  -d '{ "status": "CANCELLED", "reason": "Customer changed their mind" }'
```
- `409 Conflict` (`ILLEGAL_STATUS_TRANSITION`) for an illegal transition (e.g. `CREATED → SHIPPED`).
- `400 Bad Request` (`MISSING_TRANSITION_DATA`) if cancelling without a `reason`.

### Delete an order
```bash
curl -X DELETE http://localhost:8080/orders/{id}
```
Returns `204 No Content` or `404` if the order doesn't exist.

### Error response format
All errors share a consistent shape:
```json
{
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "details": {
    "customerName": "must not be blank",
    "items": "must not be empty"
  }
}
```
`details` is omitted (`null`) for errors that aren't field-level, e.g. `ORDER_NOT_FOUND` or `ILLEGAL_STATUS_TRANSITION`.

| HTTP Status | Code ->  When                                                                               |
|-------------|---------------------------------------------------------------------------------------------|
| 400         | `VALIDATION_ERROR` -> Bean Validation failure on request body                               |
| 400         | `INVALID_PARAMETER` -> Path/query parameter has the wrong type (e.g. non-UUID id)           |
| 400         | `MALFORMED_REQUEST` -> Request body isn't valid JSON                                        |
| 400         | `MISSING_TRANSITION_DATA` -> A status transition is missing required data                   |
| 400         | `INVALID_SORT_KEY` -> Unknown value for the `sort` query parameter                          |
| 404         | `ORDER_NOT_FOUND` -> No order with the given ID                                             |
| 409         | `ILLEGAL_STATUS_TRANSITION` -> Requested status change isn't allowed from the current state |
| 409         | `ITEMS_IMMUTABLE` -> Attempt to modify line items after the order has been paid             |
| 500         | `INTERNAL_ERROR` -> Unexpected server error (generic message; no internal details leaked)   |

### Assumptions
- `customerName` stays editable at any status; only line items become immutable after payment.
- Currency is Indonesian Rupiah (IDR), implicitly. No currency field is stored or accepted, since the spec's reference payloads never include one. Because IDR has no commonly used fractional denomination, `unitPrice` is validated as a whole number rather than the ≤2-decimal rule typical for currencies like USD.
- Amounts aren't rounded. `BigDecimal` throughout.
- Delete is a hard delete.
- Limits are reasonable guardrails.

### Scope omitted
No auth, no containerization, no optimistic locking (`@Version`) 

### How Part 2 shaped Part 1
After reading Part 2, I made a few design decisions early so I wouldn't need to refactor much later.

1. **State pattern for status transitions**  
   I used the State pattern so each `OrderStatus` has its own `OrderState` class to handle transition rules and any required validation. This makes it easy to add new transition rules without changing existing code.

2. **Strategy-style enum for sorting**  
   I used the `OrderSortKey` enum to keep each sorting rule in one place. Adding a new sort option only requires adding another enum constant, without changing the service or controller.

Because of these choices, implementing Part 2 only required adding new behavior instead of changing the existing `Order` and `OrderService` APIs or rewriting tests.

### Optional Assessments
 
**Architecture**

I implemented the State pattern (OrderState and its subclasses) to handle order status transitions, and the Strategy pattern (OrderSortKey) to handle sorting. 
I chose these patterns because both the status rules and sorting options are expected to grow over time, making it easy to add new behavior without changing existing code.

**Robustness** 

I implemented these validation and business-rule boundaries to improve the application's robustness, along with comprehensive tests. The enforced boundaries are users cannot:

- Submit an order with an empty item list.
- Specify negative, zero, non-integer, or over-limit quantities.
- Specify negative prices or prices with more than two decimal places.
- Provide blank or overlong customer or product names.
- Submit more than the maximum allowed number of line items.
- Perform illegal status transitions (e.g. ship before payment, reactivate after delivery, or cancel after shipment).
- Cancel an order without providing a cancellation reason.
- Modify line items after an order has been paid.
- Access, update, delete, or change the status of a non-existent order.

### What I'd improve given more time
- Containerize the application with a `Dockerfile` and Docker Compose for  deployment.
- Replace the current in-memory H2 database with a persistent database.
- Improve test coverage & edge cases