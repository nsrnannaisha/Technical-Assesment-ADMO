# Technical Test – Nisrina Annaisha Sarnadi
### Backend Engineer Intern – PT Astra Digital Mobil

# Order Service
A backend service for a simple e-commerce shop: create orders, manage lifecycle.

## Versions

| Component | Version |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 3.5.4 |
| Build tool | Gradle (Kotlin DSL) |
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
This runs the full suite in a single command: controller tests (`@WebMvcTest`), repository tests (`@DataJpaTest`), and pure unit tests for the entity/service layers (no Spring context). All tests use H2 in-memory, so no external setup is needed.

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

### Read a single order
```bash
curl http://localhost:8080/orders/{id}
```
Returns `404 Not Found` with a structured error body if the id doesn't exist.

### List orders (paginated, sortable)
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
      { "productName": "Apple", "quantity": 5, "unitPrice": 0.50 }
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
Returns `204 No Content`, or `404` if the order doesn't exist.

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

| Code | HTTP status | When |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Bean Validation failure on request body |
| `INVALID_PARAMETER` | 400 | Path/query param has the wrong type (e.g. non-UUID id) |
| `MALFORMED_REQUEST` | 400 | Request body isn't valid JSON |
| `MISSING_TRANSITION_DATA` | 400 | A status transition is missing required data (e.g. cancel without reason) |
| `INVALID_SORT_KEY` | 400 | Unknown value for the `sort` query param |
| `ORDER_NOT_FOUND` | 404 | No order with the given id |
| `ILLEGAL_STATUS_TRANSITION` | 409 | Requested status change isn't allowed from the current state |
| `ITEMS_IMMUTABLE` | 409 | Attempt to modify line items after the order has been paid |
| `INTERNAL_ERROR` | 500 | Unexpected server error (message is generic; no internals leaked) |

### Assumptions
- **`totalAmount` is server-computed** (`Σ quantity × unitPrice`), not client-supplied — removes "total doesn't match line items" as a possible input state entirely.
- **`orderId`, `status`, `totalAmount` can't be set by the client** — they simply aren't fields on the request DTOs, so there's nothing to strip or block.
- **`customerName` stays editable at any status**; only line items become immutable after payment, per spec.
- **Persistence is in-memory (H2)** — survives across requests, not across an app restart. Satisfies the literal requirement; see improvements below.
- **Amounts aren't rounded** — `BigDecimal` throughout, validated to ≤2 decimal places on input, to avoid compounding rounding errors.
- **Delete is a hard delete** — no soft-delete/archive flag.
- **Limits** (qty ≤ 10,000, ≤100 items/order, names ≤255 chars) are reasonable guardrails, left to my discretion per spec.
### Scope omitted
No auth, no containerization, no optimistic locking (`@Version`) 
 
### How Part 2 shaped Part 1
Reading Part 2 first led to two structural choices up front instead of rework later:
1. **State pattern for status transitions** — each `OrderStatus` has an `OrderState` implementation owning both legal transitions (`canTransitionTo`) and per-transition data requirements (`validateTransitionData`, e.g. `CancelledState` requires a `reason`). Since Part 2 says transition-data rules "will continue to be added," a new rule is a one-line override in one class, touching nothing else.
2. **Strategy-style enum for sorting** — `OrderSortKey` gives each ordering rule its own `comparator()`. A new rule is a new enum constant; existing rules are untouched.
Because of this, Part 2's actual requirements added no changes to `Order`/`OrderService` method signatures, and extended rather than rewrote existing tests.
 
### Optional extensions
 
**Architecture**

The State pattern (`OrderState`/subclasses/`OrderStateFactory`) and Strategy pattern (`OrderSortKey` enum) above, chosen specifically because Part 2 says both transition rules and ordering rules will keep growing.
 
**Robustness** - explicitly rejected and tested:
- Empty item list
- Negative/zero/non-integer quantity
- Quantity over limit
- Negative price / price with >2 decimals
- Blank or overlong names
- Too many items
- Illegal status transitions (ship-before-pay, reactivate-after-delivery, cancel-after-ship)
- Cancel without reason
- Item changes after payment 
- Unknown order id, on every endpoint
 
### What I'd improve given more time
- Move off in-memory H2 to a persistent/containerized DB, plus a `Dockerfile`.
- **Soft-delete orders** instead of hard-deleting them, for audit-trail purposes appropriate to an e-commerce order history.
