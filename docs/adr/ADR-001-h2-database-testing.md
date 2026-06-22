# ADR-001: H2 Database for Testing and Migration to Testcontainers PostgreSQL

**Status: Proposed**

## 1. Context

Currently, the IT Workshop Ticket Booking Platform utilizes an in-memory H2 database (with PostgreSQL compatibility mode) for local development and Continuous Integration/Continuous Deployment (CI/CD) testing environments. This approach was initially chosen for its simplicity, speed, and ease of setup, especially during the early stages of development.

While H2 provides a quick feedback loop for unit and integration tests, there is a recognized risk of dialect mismatch and behavioral discrepancies when comparing against the production PostgreSQL database. H2's PostgreSQL compatibility mode is not 100% equivalent to a real PostgreSQL instance, which can lead to subtle bugs or unexpected behavior in production that were not caught during testing.

## 2. Decision

For the initial sprint and immediate development needs, we accept the use of the H2 in-memory database with PostgreSQL compatibility mode for testing. This decision prioritizes rapid development and a quick start.

However, it is a **mandatory future task** to migrate our testing strategy to use **Testcontainers with a real PostgreSQL instance**.

## 3. Consequences / Future Plan

### Short-Term (Current Sprint)
*   **Pros**: Fast test execution, minimal setup overhead for developers and CI/CD, and accelerates initial feature delivery.
*   **Cons**: Potential for subtle database-related bugs due to H2-PostgreSQL dialect differences. Risk of false positives or negatives in tests.

### Long-Term (Next Sprint)
*   **Migration**: We will schedule a dedicated task in the next sprint to refactor our test setup to incorporate **Testcontainers PostgreSQL**.
*   **Benefits of Testcontainers**: This migration will provide a true-to-production database environment for all tests, guaranteeing 100% database parity between development, testing, and production. This eliminates dialect mismatch issues and increases confidence in the deployed application.
*   **Development Impact**: Developers will need Docker running to execute integration tests locally, which is already a prerequisite for local PostgreSQL setup.
*   **CI/CD Impact**: CI/CD pipelines will be updated to leverage Testcontainers, ensuring that builds are validated against an environment identical to production.

This decision balances immediate development velocity with a commitment to long-term stability and reliability.
