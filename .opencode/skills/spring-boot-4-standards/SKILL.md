---
name: spring-boot-4-standards
description: Spring Boot 4 core features, Jakarta EE 11 migration, and Modulith architecture standards
---

# Spring Boot 4 Standards

## 1. Jakarta EE 11 Migration - ⚠️ BREAKING

**javax.* → jakarta.* namespace** 

```java
// ❌ OLD - Spring Boot 3.x
import javax.servlet.http.HttpServletRequest;
import javax.persistence.Entity;
import javax.validation.Valid;
import javax.annotation.PostConstruct;

// ✅ NEW - Spring Boot 4.0+
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.Entity;
import jakarta.validation.Valid;
import jakarta.annotation.PostConstruct;
```

**Key dependency changes:**
- Servlet 6.1 (Tomcat 11.0 / Jetty 12.1)
- JPA 3.2 (Hibernate ORM 7.1/7.2)
- Bean Validation 3.1 (Hibernate Validator 9.0+)
- Undertow removed (doesn't support Servlet 6.1)

## 2. JSpecify Null Safety - ✅ NEW

**Portfolio-wide null safety improvements**

```java
// ✅ GOOD - Explicit nullability
import org.jspecify.annotations.Nullable;
import org.jspecify.annotations.NonNull;

@Service
public class OrderService {
    public @NonNull OrderDto getOrder(@Nullable String id) {
        if (id == null) return OrderDto.empty();
        return repository.findById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
    }
}

// ❌ BAD - No null annotations
public OrderDto getOrder(String id) {
    // Null safety not enforced
}
```

## 3. Modularized Codebase - ✅ NEW

**Spring Boot itself is now modular with smaller, focused JARs**

```xml
<!-- Maven: Use specific modules instead of huge dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<!-- More targeted JARs, no unnecessary transitive dependencies -->
```

## 4. Spring Modulith - Architecture Standard

**Structure modules as DDD Bounded Contexts**

```
com.example.myapp/
├── MyApplication.java          # @SpringBootApplication, root package
├── shipment/                    # Module: Shipment
│   ├── ShipmentService.java     # Public API
│   ├── internal/
│   │   ├── ShipmentRepository.java
│   │   └── ShipmentServiceImpl.java
│   ├── dto/                     # Module DTOs (records)
│   └── event/                   # Domain events
├── customer/                    # Module: Customer
│   └── ...
└── shared/                      # Shared modules (security, DTO)
```

**Key rules:**
- Modules = direct sub-packages of main package
- No circular dependencies
- Internal packages NOT referenced by other modules
- Communication via exposed APIs only

## 5. @ApplicationModuleListener - Event-Driven

```java
// ✅ GOOD - Asynchronous event handling
@ApplicationModuleListener
@Async
public void handleOrderCreated(OrderCreatedEvent event) {
    // Process event asynchronously
    notificationService.sendConfirmation(event.orderId());
}

// ✅ GOOD - Publish events within module
@Service
public class ShipmentService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void createOrder(ShipmentRequest request) {
        Shipment shipment = repository.save(toEntity(request));
        eventPublisher.publishEvent(
            new ShipmentCreatedEvent(shipment.getId())
        );
    }
}
```

## 6. Module Verification - CI/CD Must Pass

```java
// ✅ GOOD - Verify module structure in tests
@Test
void verifyModulithStructure() {
    ApplicationModules modules = ApplicationModules.of(MyApplication.class);
    modules.verify();  // Fails on invalid dependencies
}

// Generate PlantUML documentation
@Test
void writeModuleDocumentation() {
    ApplicationModules.of(MyApplication.class)
        .forEach(module -> System.out.println(module.getName()));
}
```

## 7. Configuration Properties with Records

```java
// ✅ GOOD - Type-safe with records
@ConfigurationProperties(prefix = "app.payment")
public record PaymentProperties(
    String apiUrl,
    int timeout,
    RetryPolicy retry
) {}

// ✅ GOOD - Enable properties
@Configuration
@EnableConfigurationProperties(PaymentProperties.class)
public class PaymentConfig {
    // Configuration
}
```

## 8. HTTP Service Clients - ✅ NEW

**Built-in REST client support**

```java
// ✅ GOOD - Declarative HTTP client
@HttpExchange("/api/orders")
public interface OrderClient {
    @GetExchange("/{id}")
    OrderDto getOrder(@PathVariable String id);
    
    @PostExchange
    OrderDto createOrder(@RequestBody CreateOrderRequest request);
}

// Use in service
@Service
public class OrderService {
    private final OrderClient orderClient;  // Auto-configured
}
```

## 9. API Versioning - ✅ NEW

```java
// ✅ GOOD - Versioned REST endpoints
@RestController
@ApiVersion("v1")
public class OrderControllerV1 {
    @GetMapping("/orders/{id}")
    public OrderV1Dto getOrder(@PathVariable String id) { /* ... */ }
}

@RestController
@ApiVersion("v2")
public class OrderControllerV2 {
    @GetMapping("/orders/{id}")
    public OrderV2Dto getOrder(@PathVariable String id) { /* ... */ }
}
```

## 10. Java 25 Compatibility

**Supports Java 25 LTS while maintaining Java 17 compatibility**

```java
// ✅ GOOD - Use Java 25 features safely
public record OrderStatusUpdate(
    String orderId,
    @NotNull @Positive BigDecimal amount,
    LocalDateTime timestamp
) {}

// ✅ GOOD - Pattern matching in services
if (response instanceof OrderCreatedEvent event) {
    handleCreatedEvent(event);
}
```

## 11. Testing Standards

```java
// ✅ GOOD - Test with modulith verification
@SpringBootTest
@ModulithTest
class ApplicationTests {
    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(MyApplication.class).verify();
    }
}
```

## 12. Key Dependency Versions

| Dependency | Version |
|-----------|---------|
| Spring Boot | 4.0.0+ |
| Spring Framework | 7.0.0+ |
| Jakarta EE | 11 (Servlet 6.1, JPA 3.2, Validation 3.1) |
| Tomcat | 11.0.14+ |
| Hibernate | 7.1.8+ |
| Micrometer | 1.16.0+ |
| Spring Security | 7.0.0+ |
| Spring Data | 2025.1.0+ |
| Testcontainers | 2.0.2+ |

## Forbidden Behaviors

- ❌ Use javax.* imports (migrate to jakarta.*)
- ❌ Cross-module internal package references
- ❌ Circular dependencies between modules
- ❌ Bypass Spring Modulith verification
- ❌ Ignore @Nullable/@NonNull annotations
- ❌ Use Undertow (removed in Boot 4)