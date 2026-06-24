---
name: java-25-standards
description: Java 25 LTS core language features and coding standards for Spring Boot 4 Modulith projects
---

# Java 25 Standards

## 1. Compact Source Files (JEP 512) - ✅ Final

**Use for scripts and simple programs only, NOT for production classes**

```java
// ✅ For demos/scripts - no class declaration needed
void main() {
    IO.println("Hello, World!");  // java.lang.IO
}

// ❌ For production - keep explicit class declaration
public class OrderService {
    // ...
}
```
2. Flexible Constructor Bodies (JEP 513) - ✅ Final

Validate before calling super()
```java

// ✅ GOOD - Validation before super
class Employee extends Person {
    final String payrollId;

    Employee(String name, String payrollId) {
        // Prologue: validate before super
        if (payrollId == null || !payrollId.matches("EMP-\\d{4}")) {
            throw new IllegalArgumentException("Invalid payrollId");
        }
        this.payrollId = payrollId;  // Allowed before super
        super(name);  // super after validation
    }
}

// ❌ BAD - Validation after super (risk of partial state)
class Employee extends Person {
    Employee(String name, String payrollId) {
        super(name);  // must be first line in old Java
        if (payrollId == null) throw ...
        this.payrollId = payrollId;
    }
}
```
3. Primitive Patterns in instanceof (JEP 507) - 🧪 Preview

Safe type checking without silent data loss
```java

// ✅ GOOD - Exact conversion check
if (obj instanceof int exact) {
    // Only executes if obj can be exactly converted to int
    return processInt(exact);
}

// ❌ BAD - Manual cast with silent truncation risk
if (obj instanceof Double d) {
    int i = d.intValue();  // May silently lose precision
}
```
Use in switch:
```java

// ✅ GOOD - Pattern matching with guards
switch (i) {
    case int v when v < 1 -> "FREE";
    case int v when v < 1_000 -> "STARTER";
    case int v when v < 10_000 -> "PRO";
    case int v -> "ENTERPRISE";
}

// ✅ GOOD - All primitive wrapper types now supported
Double d = 20.0;
switch(d) {
    case 20.0 -> "Perfect match";
    default -> "No match";
}
```
4. Records - Use for DTOs and Immutable Data
```java

// ✅ GOOD - Immutable DTO
public record CreateOrderRequest(
    String customerId,
    List<OrderItem> items,
    @NotNull @Positive BigDecimal amount
) {}

// ❌ BAD - Mutable class for data carrier
public class CreateOrderRequest {
    private String customerId;
    // getters/setters...
}
```
5. Text Blocks - Use for SQL, JSON, Multi-line Strings
```java

// ✅ GOOD - Readable SQL
String sql = """
    SELECT o.id, o.amount, c.name 
    FROM orders o 
    JOIN customers c ON o.customer_id = c.id
    WHERE o.status = 'PENDING'
    """;

// ❌ BAD - String concatenation
String sql = "SELECT o.id, o.amount, c.name " +
             "FROM orders o " +
             "JOIN customers c ON o.customer_id = c.id";
```
6. Module Import Declarations (JEP 511) - ✅ Final

For prototyping - use sparingly in production
```java

// ✅ For quick prototyping
import module java.base;
import module java.net.http;

// ❌ Avoid in production - can cause ambiguous imports
import module java.base;
import module java.sql;
// Both have Date class → compile error!
```
Resolution: Use specific import for ambiguity:
```java

import module java.base;
import module java.sql;
import java.sql.Date;  // Resolve ambiguity

public class Main {
    Date d = Date.valueOf("2025-06-15");
}
```
7. Scoped Values (JEP 506) - ✅ Final

Thread-safe alternative to ThreadLocal for virtual threads
```java

// ✅ GOOD - ScopedValue for request context
public class RequestContext {
    static final ScopedValue<String> USER_ID = ScopedValue.newInstance();
    static final ScopedValue<UUID> CORRELATION_ID = ScopedValue.newInstance();
}

// Usage
ScopedValue.where(RequestContext.USER_ID, "user-123")
    .run(() -> {
        // All code here can access USER_ID safely
        String userId = RequestContext.USER_ID.get();
        processRequest(userId);
    });

// ❌ AVOID - ThreadLocal with virtual threads
private static final ThreadLocal<String> user = new ThreadLocal<>();
```
8. Structured Concurrency (JEP 505) - 🧪 Preview

Manage related tasks as a unit
```java

// ✅ GOOD - Structured task scope
try (var scope = StructuredTaskScope.<String>open()) {
    var userTask = scope.fork(() -> fetchUser());
    var orderTask = scope.fork(() -> fetchOrder());
    
    scope.join();  // Wait for all tasks
    
    String user = userTask.get();
    String order = orderTask.get();
    return user + " - " + order;
}
// Tasks automatically cancelled if scope closes
```
9. Best Practices (Based on OpenRewrite Java 25)

Use these modern patterns:

| Old Pattern                                      | Java 25 Pattern                          |
| ------------------------------------------------ | ---------------------------------------- |
| StringBuilder for simple concat                  | Text blocks or `String.formatted()`      |
| Stack class                                      | Deque interface                          |
| System.gc() calls                                | Remove - JVM manages GC                  |
| new ArrayList<>() with explicit type             | Use `var` with diamond operator          |
| if (x != null && x instanceof String)            | if (x instanceof String s)               |
| "some string".replace("a", "b")                  | "some string".replace('a', 'b')          |
| URL.equals() for comparison                      | Use URI or custom logic                  |
| catch (ExceptionA \| ExceptionB)                 | Multi-catch with same handling           |

10. Memory & Performance (Java 25 LTS)

    Compact Object Headers: 8-15% heap reduction

    Generational Shenandoah: Sub-10ms pause times

    Use var for local variables with clear types on RHS

    Prefer records over mutable POJOs

11. Null Safety with JSpecify

Spring Boot 4 uses JSpecify for null safety :
```java

// ✅ GOOD - Explicit nullability
import org.jspecify.annotations.Nullable;

public class OrderService {
    public OrderDto getOrder(@Nullable String id) {
        if (id == null) return null;
        return repository.findById(id);
    }
}
```
12. Key Version Requirements

    | Component | Version |
    | --- | --- |
    | Java | 25 LTS |
    | Spring Boot | 4.0.0+ |
    | Spring Framework | 7.0.0+ |
    | Maven | 3.6.3+ |
    | Gradle | 8.14+ |
    | Servlet | 6.1+ (Jakarta EE 11) |
    | Lombok | 1.18.36+ |