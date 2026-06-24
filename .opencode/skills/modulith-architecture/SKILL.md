---
name: modulith-architecture
description: Spring Modulith architecture standards - module boundaries, event-driven communication, and testing for modular monoliths
---

# Modulith Architecture Standards

## 1. Core Concept - Package-by-Module

**Spring Modulith treats direct sub-packages of the main application package as modules**.

```
com.example.bookstore/                 # Main package (@SpringBootApplication)
├── BookstoreApplication.java
├── catalog/                           # Module: Catalog
│   ├── ProductApi.java                # Public API (exposed)
│   ├── domain/                        # Internal (private)
│   └── web/                           # Internal (private)
├── orders/                            # Module: Orders
│   ├── OrderManagement.java           # Public API
│   └── internal/                      # Hidden from other modules
│       ├── OrderService.java
│       └── OrderRepository.java
└── inventory/                         # Module: Inventory
└── ...
```

**Key principle:** Top-level packages = application modules. Internal packages (`internal`) are NOT accessible from other modules.

## 2. Module Metadata - package-info.java

**Each module declares dependencies via `package-info.java`**:

```java
// orders/package-info.java
@ApplicationModule(
    allowedDependencies = {"catalog", "common"}  // Whitelist
)
package com.example.bookstore.orders;

import org.springframework.modulith.ApplicationModule;
```

**Rules:**
- `allowedDependencies = {}` → NO dependencies allowed (isolated module)
- Only explicitly listed modules can be accessed
- **Cyclic dependencies are strictly forbidden**

## 3. Named Interfaces - Explicit Public API

**Use `@NamedInterface` to define what other modules can consume**:

```java
// catalog/ProductApi.java - Public API
@NamedInterface("product")
public interface ProductApi {
    ProductDto getProduct(String id);
    List<ProductDto> findProducts(ProductSearchRequest request);
}

// catalog/ProductServiceImpl.java - Internal implementation (package-private)
@Service
class ProductServiceImpl implements ProductApi {
    // NOT accessible from outside
}
```

**Usage in allowed dependencies:**
```java
@ApplicationModule(
    allowedDependencies = {"catalog::product", "common"}  // Only ProductApi
)
package com.example.bookstore.orders;
```

## 4. Event-Driven Communication - @ApplicationModuleListener

**Modules communicate via events, NOT direct bean injection across boundaries**.

```java
// ✅ GOOD - Publish event
@Service
public class OrderService {
    private final ApplicationEventPublisher eventPublisher;
    
    public OrderDto createOrder(CreateOrderRequest request) {
        Order order = repository.save(toEntity(request));
        eventPublisher.publishEvent(
            new OrderCreatedEvent(order.getId(), order.getAmount())
        );
        return toDto(order);
    }
}

// ✅ GOOD - Handle event from another module
@Service
public class InventoryService {
    @ApplicationModuleListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Process asynchronously in separate transaction
        inventoryRepository.reserveStock(event.orderId(), event.amount());
    }
}
```

**`@ApplicationModuleListener` behavior**:
- **Async**: Runs in separate thread pool
- **Independent transaction**: Uses `REQUIRES_NEW` propagation
- **After commit**: Only triggers after publisher's transaction commits
- **Result**: Modules maintain **eventual consistency**, not strong consistency

## 5. Event Publication Registry - Reliability

**Enable at-least-once delivery for events**:

```xml
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-jdbc</artifactId>  <!-- or jpa -->
</dependency>
```

**Effect:**
- Events persisted to `event_publication` table
- Retry on listener failure
- Prevents event loss on application/ listener failure

## 6. Runtime Verification - CI Enforced

**Add modularity test that FAILS on violations**:

```java
@SpringBootTest
class ModularityTest {
    static ApplicationModules modules = 
        ApplicationModules.of(BookstoreApplication.class);
    
    @Test
    void verifyModularStructure() {
        modules.verify();  // Fails CI if boundaries violated
    }
}
```

**Violations caught by verify():**
- **Cycles**: No circular dependencies between modules
- **Internal access**: Cannot import types from other modules' internal packages
- **Allowed dependencies**: Only modules listed in `allowedDependencies` can be used
- **ArchUnit integration**: Hexagonal/Clean Architecture verification if `jMolecules` on classpath

## 7. Auto-Generated Documentation

**Documenter generates architecture diagrams from code**:

```java
@Test
void writeDocumentationSnippets() {
    var modules = ApplicationModules.of(Application.class).verify();
    new Documenter(modules)
        .writeModulesAsPlantUml()          // C4 component diagram
        .writeIndividualModulesAsPlantUml();  // Per-module structure
}
```

**Output**: `target/spring-modulith-docs/` contains:
- Component diagrams (PlantUML)
- Application module canvas
- C4-style architecture documentation

## 8. Module Testing - @ApplicationModuleTest

**Test modules in isolation without loading entire application**:

```java
@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.HEADLESS)
class OrderModuleTest {
    @Test
    void verifyOrderFlow(ApplicationModuleTest.TestModule module) {
        // Loads ONLY orders module and its dependencies
        // Other modules mocked
    }
}
```

## 9. Runtime Support - ApplicationModuleInitializer

**Module-specific startup initialization with dependency-aware order**:

```java
@Component
class DatabaseInitializer implements ApplicationModuleInitializer {
    @Override
    public void initialize() {
        // Runs BEFORE dependent modules' initializers
        initializeDatabaseSchema();
    }
}
```

**Execution order**: Automatically follows module dependency graph.

---

## Forbidden Behaviors

- ❌ **Cyclic dependencies** between modules
- ❌ **Cross-module internal imports** (accessing `internal` packages)
- ❌ **Direct bean injection** between modules (use events instead)
- ❌ **Bypassing `allowedDependencies`** whitelist
- ❌ **Strong consistency** across module boundaries (use events + eventual consistency)
- ❌ **Ignoring verification test failures** in CI
---