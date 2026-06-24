---
name: testing-standards-java
description: Testing standards for Java with JUnit 5/6, Mockito, and Spring Boot 4/Modulith - covers unit, integration, and slice testing
---

# Testing Standards - Java + Spring Boot 4

## 1. Core Principles

- **Test the behavior, not the implementation**
- **Tests must be deterministic and isolated**
- **Follow AAA pattern** (Arrange, Act, Assert)
- **Cover both happy path and edge cases**
- **Tests are documentation** — must be readable

## 2. Test Levels & Scope

| Level | Scope | Tools | When |
|-------|-------|-------|------|
| **Unit** | Single class, mocked dependencies | JUnit 5 + Mockito | Every method |
| **Slice** | Web/Repository layer only | `@WebMvcTest`, `@DataJpaTest` | Controller/Repository logic |
| **Integration** | Multiple components, real DB | `@SpringBootTest`, Testcontainers | End-to-end flows |
| **Modulith** | Module boundaries verification | `ApplicationModules.verify()` | Architecture integrity |

**Decision Tree**:
- Testing business logic? → **Unit test** with `MockitoExtension`
- Testing controller endpoint? → `@WebMvcTest` with `MockMvc`
- Testing repository queries? → `@DataJpaTest` with Testcontainers
- Testing external API client? → `@RestClientTest`
- Need full integration? → `@SpringBootTest`

## 3. JUnit 5/6 Fundamentals

**Key annotations for well-structured tests**:

```java
// ✅ GOOD - Well-structured test
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @InjectMocks
    private OrderService service;

    @BeforeEach
    void setUp() {
        // Fresh setup per test
    }

    @Test
    @DisplayName("Should create order with valid request")
    void shouldCreateOrderWithValidRequest() {
        // Arrange
        var request = new CreateOrderRequest("customer-123", BigDecimal.TEN);
        var expectedOrder = new Order("order-456", BigDecimal.TEN);
        when(repository.save(any(Order.class))).thenReturn(expectedOrder);

        // Act
        var result = service.createOrder(request);

        // Assert
        assertAll(
            () -> assertNotNull(result.id()),
            () -> assertEquals(BigDecimal.TEN, result.amount()),
            () -> verify(repository).save(any(Order.class))
        );
    }
}
```

**Annotate tests for clarity**: use `@DisplayName` for human-readable test names and `@Tag` for test categorization.

**Use `@Nested`** to group 3+ related tests for better organization and hierarchical reporting.

## 4. Unit Testing with Mockito

**Use Mockito to isolate the class under test**:

```java
// ✅ GOOD - Isolated unit test
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentGateway gateway;

    @Mock
    private AuditService audit;

    @InjectMocks
    private PaymentService service;

    @Test
    void shouldProcessPaymentSuccessfully() {
        // Arrange
        when(gateway.charge(any(PaymentRequest.class)))
            .thenReturn(new PaymentResult("tx-123", Status.SUCCESS));

        // Act
        var result = service.process(new PaymentRequest(100.0));

        // Assert
        assertTrue(result.success());
        verify(audit).logPayment(any());
        verifyNoMoreInteractions(gateway);
    }

    @Test
    void shouldThrowWhenGatewayFails() {
        // Arrange
        when(gateway.charge(any())).thenThrow(new GatewayException("timeout"));

        // Act & Assert
        assertThrows(PaymentException.class, 
            () -> service.process(new PaymentRequest(100.0)));
    }
}
```

**Key Mockito patterns**:
- `@Mock` → create mock; `@InjectMocks` → inject mocks into tested class
- `when().thenReturn()` → stub behavior
- `verify()` → assert interactions
- `verifyNoMoreInteractions()` → ensure no unexpected calls

## 5. Slice Testing in Spring Boot 4

**Spring Boot 4** requires explicit test starter dependencies due to modularization:

```xml
<!-- Spring Boot 4.0: Explicit test starters required -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-data-jpa-test</artifactId>
    <scope>test</scope>
</dependency>
```

### Controller Testing with MockMvc

```java
// ✅ GOOD - Controller slice test
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean  // New in Boot 4, replaces @MockBean
    private OrderService service;

    @Test
    @DisplayName("GET /orders/{id} returns 200 with order")
    void shouldReturnOrder() throws Exception {
        var order = new OrderDto("order-123", BigDecimal.TEN);
        when(service.getOrder("order-123")).thenReturn(order);

        mockMvc.perform(get("/orders/{id}", "order-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("order-123"))
            .andExpect(jsonPath("$.amount").value(10.0));
    }
}
```

### Repository Testing

```java
// ✅ GOOD - Repository slice test
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository repository;

    @Test
    void shouldFindByCustomerId() {
        var order = new Order("customer-123", BigDecimal.TEN);
        repository.save(order);

        var found = repository.findByCustomerId("customer-123");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getAmount()).isEqualTo(BigDecimal.TEN);
    }
}
```

## 6. RestTestClient (Spring Boot 4+)

**Modern alternative to TestRestTemplate** with fluent API:

```java
// ✅ GOOD - Using RestTestClient
@AutoConfigureRestTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApiIT {

    @Autowired
    private RestTestClient restClient;

    @Test
    void shouldReturnHealthStatus() {
        restClient
            .get().uri("/actuator/health")
            .exchange()
            .expectStatus().is2xxSuccessful()
            .expectBody().jsonPath("$.status").isEqualTo("UP");
    }
}
```

## 7. Modulith Testing

**Verify module boundaries in CI**:

```java
// ✅ GOOD - Modularity verification test
@SpringBootTest
class ModularityTest {
    static ApplicationModules modules = 
        ApplicationModules.of(Application.class);

    @Test
    void verifyModularStructure() {
        modules.verify();  // Fails on dependency violations
    }

    @Test
    void generateDocumentation() {
        new Documenter(modules)
            .writeModulesAsPlantUml()
            .writeIndividualModulesAsPlantUml();
    }
}
```

## 8. Testcontainers 2.0 for Integration Tests

**New dependency structure** (Spring Boot 4+):

```xml
<!-- Testcontainers 2.0: use testcontainers- prefix -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

```java
// ✅ GOOD - Integration with Testcontainers
@SpringBootTest
@Container
class OrderRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = 
        new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb");

    @Test
    void shouldPersistOrder() {
        // Real database operation with Testcontainers
    }
}
```

## 9. Test Context Caching (Spring Framework 7+)

**Spring Framework 7 automatically pauses cached contexts** to prevent resource conflicts from background threads:

```java
@Component
public class ScheduledLogger {
    @Scheduled(fixedDelay = 100L)
    public void log() {
        // Automatically paused when context is cached
        // Resumes when context is reused
    }
}
```

## 10. Testing Java 25 Features

**Use Java 25 features safely in tests**:

```java
// ✅ GOOD - Using records in tests
public record OrderTestData(String id, BigDecimal amount) {
    static OrderTestData create(String id) {
        return new OrderTestData(id, BigDecimal.TEN);
    }
}

// ✅ GOOD - Pattern matching in tests
@Test
void shouldMatchOrderStatus() {
    Object status = new OrderStatus("COMPLETED");
    if (status instanceof OrderStatus s) {
        assertEquals("COMPLETED", s.value());
    }
}
```

## 11. Coverage Standards

**Aim for 80% line and branch coverage** (critical paths: near 100%):

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <configuration>
        <rules>
            <rule>
                <element>PACKAGE</element>
                <limits>
                    <limit>
                        <counter>LINE</counter>
                        <value>COVEREDRATIO</value>
                        <minimum>0.80</minimum>
                    </limit>
                </limits>
            </rule>
        </rules>
    </configuration>
</plugin>
```

**Priority order for testing**:
1. Happy path (main scenario)
2. Alternative valid paths
3. Edge cases (boundary values)
4. Exceptions/Errors (invalid inputs)

## 12. Best Practices Checklist

### Do's
- ✅ Test only the code you wrote (mock external dependencies)
- ✅ Use `@DisplayName` for readable test descriptions
- ✅ Structure tests: Arrange → Act → Assert (no phase comments needed)
- ✅ Use `assertAll` for grouped assertions to see all failures at once
- ✅ Test edge cases (null, empty, boundary values, exceptions)
- ✅ Keep tests independent (no shared state between tests)
- ✅ Run Maven/Gradle builds with tests: `mvn clean verify` or `./gradlew test`

### Don'ts
- ❌ Don't use `Thread.sleep()` in tests — use Awaitility instead
- ❌ Don't test the framework (Spring, JPA) — test your code
- ❌ Don't use `@MockBean` in unit tests — use `@ExtendWith(MockitoExtension.class)`
- ❌ Don't skip test execution in CI
- ❌ Don't use `@SpringBootTest` for simple unit tests — loads unnecessary context

---

## Key Dependencies (Spring Boot 4+)

| Dependency | Version | Purpose |
|------------|---------|---------|
| `junit-jupiter` | 6.0+ | JUnit 6 core |
| `mockito-junit-jupiter` | 5.20+ | Mockito + JUnit 5 integration |
| `spring-boot-starter-webmvc-test` | 4.0+ | Controller testing (`MockMvc`) |
| `spring-boot-data-jpa-test` | 4.0+ | JPA repository testing (`@DataJpaTest`) |
| `spring-boot-starter-restclient-test` | 4.0+ | REST client testing (`RestTestClient`) |
| `testcontainers` | 2.0+ | Integration testing with real dependencies |
| `jacoco-maven-plugin` | 0.8+ | Coverage reporting |

---

> **"Program testing can be used to show the presence of bugs, but never to show their absence."** — Dijkstra