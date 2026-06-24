---
name: hexagonal-architecture
description: Hexagonal/DDD architecture patterns inside Spring Boot Modulith modules - domain purity, ports & adapters, and integration with Spring Modulith
---

# Hexagonal Architecture Inside Spring Boot Modulith

## 1. Core Concept

Hexagonal Architecture (Ports & Adapters) tổ chức code bên trong mỗi module, trong khi Spring Modulith kiểm soát truy cập giữa các module. Hai phong cách bổ sung cho nhau.

## 2. Structure Inside Module

```
orders/                                 ← Spring Modulith Module
├── OrderService.java                   ← API root (public)
├── internal/                           ← Hidden from other modules
│   ├── domain/                         ← Business logic (NO Spring/JPA)
│   │   ├── model/                      ← Entities, Value Objects
│   │   ├── port/                       ← Interfaces (in/out)
│   │   └── service/                    ← Domain services
│   ├── application/                    ← Orchestration
│   │   └── OrderApplicationService.java
│   └── adapter/                        ← Technical details
│       ├── in/web/                     ← Controllers
│       └── out/persistence/            ← JPA repositories
```

## 3. Domain Layer - Framework-Free

**Domain KHÔNG được import Spring/JPA**:

```java
// ✅ Domain entity (no JPA!)
public class Order {
    private final String id;
    private OrderStatus status;
    
    public void approve() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot approve non-pending order");
        }
        this.status = OrderStatus.APPROVED;
    }
}

// ✅ Port interface (no Spring!)
public interface OrderRepositoryPort {
    Order findById(String id);
    Order save(Order order);
}
```

**JPA entity để riêng trong adapter**:

```java
// ✅ JPA entity in adapter layer
@Entity
@Table(name = "orders")
public class OrderJpaEntity {
    @Id private String id;
    // JPA annotations only here
}
```

## 4. Application Layer

```java
@Transactional
@Service
@RequiredArgsConstructor
public class OrderApplicationService implements CreateOrderUseCase {
    private final OrderRepositoryPort repository;
    private final OrderDomainService domainService;
    
    @Override
    public Order createOrder(OrderCreateCommand command) {
        var order = domainService.validateAndCreate(command);
        return repository.save(order);
    }
}
```

## 5. Adapter Layer

### Inbound (Controller)
```java
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final CreateOrderUseCase createOrderUseCase;
    
    @PostMapping
    public OrderDto createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return createOrderUseCase.createOrder(toCommand(request));
    }
}
```

### Outbound (Persistence)
```java
@Repository
@RequiredArgsConstructor
public class OrderJpaAdapter implements OrderRepositoryPort {
    private final OrderJpaRepository jpaRepository;
    
    @Override
    public Order save(Order order) {
        return toDomain(jpaRepository.save(toJpaEntity(order)));
    }
}
```

## 6. Integration with Spring Modulith

| Approach | How | Use Case |
|----------|-----|----------|
| **Module root API** | `OrderManagement.java` ở root | Simple, recommended |
| **@NamedInterface ports** | `@NamedInterface` trên port packages | Fine-grained control |

## 7. Verification

```java
@SpringBootTest
class ModularityTest {
    @Test
    void verifyArchitecture() {
        ApplicationModules.of(Application.class)
            .verify(VerificationOptions.defaults()
                .withAdditionalVerifications(
                    JMoleculesArchitectureRules.ensureHexagonal()
                )
            );
    }
}
```

## 8. When to Use

| Use Hexagonal | Don't Use |
|---------------|-----------|
| Complex business logic | Simple CRUD |
| Long-term project | Prototype |
| Domain with many rules | Domain with few rules |
| Need to change technology | Single technology |

## Forbidden Behaviors

- ❌ JPA/Spring annotations in domain
- ❌ Domain importing anything outside its package
- ❌ Controllers calling repositories directly
- ❌ Business logic in controllers