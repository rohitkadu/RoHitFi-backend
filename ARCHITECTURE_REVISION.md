# RoHitFi — Enterprise Digital Banking Core
### Architectural Blueprint & Technical Revision Reference

---

## 1. Executive Summary & Architecture Style

* **Architectural Pattern:** Modular Monolith
* **Framework:** Spring Boot 3.x (Java 17/21)
* **Design Philosophy:** Domain-Driven Design (DDD) with modular segregation.
* **Why Modular Monolith over Microservices?**
  * Avoids distributed transaction overhead (no two-phase commits / Saga complexity for fundamental ledger updates).
  * In-memory method execution eliminates inter-service network latency and serialization overhead.
  * Clear module encapsulation (`auth`, `customer`, `kyc`, `account`, `transaction`, `card`, `loan`, `investment`, `manager`, `notification`) allows independent refactoring or extraction into standalone microservices if traffic scales.

---

## 2. Polyglot Persistence Layer

| Database | Technology | Modules Handled | Technical Rationale |
| :--- | :--- | :--- | :--- |
| **Relational (SQL)** | PostgreSQL (Neon Cloud) | `accounts`, `transactions`, `cards`, `loans`, `investment_holdings`, `idempotency_keys` | **ACID Compliance & Ledger Consistency.** Strict schema constraints, relational integrity (Foreign Keys), and row-level locking prevent balance anomalies. |
| **Document (NoSQL)** | MongoDB Atlas | `kyc_documents`, `audit_logs` | **Schema Flexibility & High-Throughput Ingestion.** KYC payloads accommodate variable identity formats (Aadhaar, Passport, PAN). Audit logging requires non-blocking, append-only writes without consuming relational transaction locks. |

---

## 3. Spring Boot Annotations & Design Patterns Cheatsheet

### Core Enterprise Annotations
* `@Transactional`: Enforces ACID atomicity across SQL operations. If any checked/runtime exception escapes during multi-table writes (e.g., deducting sender, crediting receiver, writing debit and credit ledger rows), PostgreSQL automatically issues a `ROLLBACK`.
* `@Transactional(propagation = Propagation.REQUIRES_NEW)`: Used in batch processing (`EmiBatchService`) to isolate single EMI payments. A failure on one customer's account will not roll back the transactions of other customers in the batch.
* `@Async`: Offloads task execution (`EmailService`) to a background `SimpleAsyncTaskExecutor` thread pool. The client receives an instantaneous `200 OK` API response while SMTP network I/O executes non-blockingly.
* `@Scheduled(cron = "0 0 1 * * *")`: Triggers automated background batch processing (overnight 1:00 AM EMI auto-deductions) without requiring incoming HTTP triggers.
* `@JsonInclude(JsonInclude.Include.NON_NULL)`: Jackson annotation applied on DTOs (`CardResponse`, `LoanResponse`) to strip `null` fields from JSON payloads, saving network bandwidth and eliminating UI client parsing bugs.

### Key Architectural Patterns
* **DTO (Data Transfer Object) Pattern:** Prevents internal JPA entities with database mappings from leaking across API boundaries.
* **Token Bucket Algorithm (Bucket4j):** Rate limiting pattern implemented in `RateLimitingService` and `RateLimitInterceptor` to restrict malicious callers to 5 requests per minute per IP.
* **Idempotent Consumer Pattern:** Prevents double-charging on network retries using `Idempotency-Key` request headers persisted in `idempotency_keys`.

---

## 4. End-to-End System Flows

### A. Fund Transfer with Idempotency & Async Alerts
1. **Client Request:** User invokes `POST /api/transactions/transfer` with header `Idempotency-Key: <UUID>`.
2. **Rate Limit Gate:** `RateLimitInterceptor` checks the client IP against Bucket4j. If exhausted, responds with `HTTP 429 Too Many Requests`.
3. **Idempotency Gate:** `IdempotencyService` queries `idempotency_keys`. If the key exists, it skips transaction execution and immediately returns the cached response payload.
4. **Transaction Boundary (`@Transactional`):**
   * Verifies sender customer ownership and balance sufficiency.
   * Decrements sender balance; increments receiver balance.
   * Generates unique transaction reference (`TXN...D` and `TXN...C`).
   * Writes debit and credit rows to `transactions`.
   * Inserts an immutable audit event to MongoDB `audit_logs`.
5. **Asynchronous Notification:** Fires `emailService.sendTransactionReceipt(...)` onto a background worker thread.
6. **Key Persistence:** Serializes and saves the final response to `idempotency_keys`.
7. **Response:** Client receives `HTTP 200 OK` with transaction details.

### B. Manager Loan Approval & Automated EMI Batch Lifecycle
1. **Application:** Customer submits `POST /api/loans/apply`. Loan record is created in status `PENDING`.
2. **Manager Approval:** Manager calls `POST /api/manager/loans/{loanId}/approve`.
   * Loan updates to `DISBURSED`.
   * Loan principal is credited to the customer account.
   * Complete 60-month amortized EMI table is generated in `loan_emis` with calculated due dates.
   * Custom HTML disbursement email is dispatched via `@Async`.
3. **Overnight Batch Engine:** 
   * `@Scheduled` cron job queries `LoanEmiRepository.findByStatusAndDueDateLessThanEqual(PENDING, today)`.
   * Iterates through pending dues using isolated transactions.
   * If balance is sufficient: deducts EMI amount, records `DEBIT` transaction, marks EMI `PAID`.
   * If balance is insufficient: updates EMI status to `LATE` and writes failure audit trail to MongoDB.

---

## 5. Security & Configuration Standards

* **Credential Management (12-Factor App):** No plaintext secrets in `application.properties`. Secrets injected via OS environment variables:
  * `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  * `SPRING_DATA_MONGODB_URI`
  * `EMAIL_USER`, `EMAIL_PASS`
* **Password Encryption:** Passwords hashed with BCrypt (`PasswordEncoder`) using salt rounds.
* **Authentication:** Stateless authentication via signed JSON Web Tokens (JWT) passed in `Authorization: Bearer <token>` headers.
* **Role-Based Access Control (RBAC):** Distinct authorities (`ROLE_CUSTOMER`, `ROLE_MANAGER`) enforced at endpoint and method levels.

---

## 6. Quick Interview Q&A Cheat Sheet

* **Q: Why did you use both PostgreSQL and MongoDB in the same project?**
  * *A:* "I implemented a polyglot persistence architecture. Financial ledgers and balances require ACID guarantees, relational integrity, and strict consistency, which PostgreSQL provides. For KYC compliance and system audit logs, document structures vary and write volume is heavy, making MongoDB Atlas ideal for flexible schema storage and high-throughput append operations."

* **Q: How do you prevent double debiting if a user clicks 'Send Money' multiple times?**
  * *A:* "I built an API idempotency layer using an `Idempotency-Key` HTTP header. Before executing transfers, the system checks if that key has already been processed. If found, it short-circuits the database update and returns the cached response, ensuring the user is never debited twice."

* **Q: How does the system handle high-volume email alerts without slowing down transfers?**
  * *A:* "I decoupled email delivery using Spring's `@Async` mechanism. When a transfer completes, the email event is dispatched to a background thread pool, allowing the HTTP response to return to the client in milliseconds without blocking on SMTP socket I/O."

* **Q: How is EMI deduction automated in production?**
  * *A:* "I used Spring `@Scheduled` cron triggers configured to run nightly batch jobs. To prevent a single failed payment from rolling back the entire batch, each EMI deduction executes inside its own isolated transaction using `Propagation.REQUIRES_NEW`."