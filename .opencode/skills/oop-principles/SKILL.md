---
name: oop-principles
description: Core OOP principles for Java - encapsulation, inheritance, polymorphism, abstraction, and SOLID with Spring Boot Modulith context
---

# OOP Principles - Java Standards

Java is a true OOP language, not a hybrid. These principles are the foundation of maintainable, scalable enterprise applications .

## 1. Four Pillars of OOP

### 1.1 Encapsulation

**Data hiding** — bundle data and methods together, restrict external access. Private fields, public/package-private getters/setters .

```java
// ✅ GOOD - Proper encapsulation
public class BankAccount {
    private double balance;          // Private field
    private final String accountId;  // Immutable identifier

    public BankAccount(String accountId, double initialBalance) {
        this.accountId = accountId;
        this.balance = initialBalance;
    }

    public void deposit(double amount) {
        if (amount > 0) balance += amount;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public double getBalance() { return balance; }  // Read-only access
    public String getAccountId() { return accountId; }
}

// ❌ BAD - Exposed internals
public class BankAccount {
    public double balance;          // Anyone can modify!
    public String accountId;
}
```

**Access Modifiers:** `public` > `protected` > (package-private/default) > `private`. Use the most restrictive level that works .

### 1.2 Inheritance

**Code reuse** — child class extends parent, inherits fields and methods. Use `extends` keyword .

```java
// ✅ GOOD - Inheritance for shared behavior
public class Vehicle {
    protected String brand;         // Accessible to subclasses
    protected int year;

    public Vehicle(String brand, int year) {
        this.brand = brand;
        this.year = year;
    }

    public void start() {
        System.out.println("Vehicle starting...");
    }
}

// ✅ GOOD - Car extends Vehicle, adds specific behavior
public class Car extends Vehicle {
    private int doors;

    public Car(String brand, int year, int doors) {
        super(brand, year);          // Call parent constructor FIRST
        this.doors = doors;
    }

    @Override
    public void start() {
        System.out.println("Car engine starting...");  // Override behavior
    }

    public void honk() {
        System.out.println("Beep beep!");
    }
}
```

**Rules:**
- Use `super()` as first line in constructor to initialize parent
- Java supports **single inheritance** for classes (multiple for interfaces)
- Private fields inherited? **No** — but accessible via public/protected getters/setters

### 1.3 Polymorphism

**"One interface, many implementations"** — same method name, different behavior depending on actual object type .

```java
// ✅ GOOD - Runtime polymorphism
class Animal {
    void speak() { System.out.println("..."); }
}

class Dog extends Animal {
    @Override void speak() { System.out.println("Woof"); }
}

class Cat extends Animal {
    @Override void speak() { System.out.println("Meow"); }
}

public class Main {
    static void makeItSpeak(Animal a) {
        a.speak();  // Works for Dog, Cat, ANY Animal!
    }

    public static void main(String[] args) {
        makeItSpeak(new Dog());  // Prints Woof
        makeItSpeak(new Cat());  // Prints Meow
    }
}
```

**Key concepts:**
- **Subtype polymorphism**: Subclass can be used where superclass expected: `Employee e = new Manager(...)`
- **Method overloading**: Same name, different parameters (compile-time)
- **`@Override`** annotation — mandatory for clarity, catches errors

### 1.4 Abstraction

**Hide complexity** — expose only what's necessary via interfaces and abstract classes .

```java
// ✅ GOOD - Interface defines contract (WHAT, not HOW)
interface Printer {
    void print(String text);
}

// ✅ GOOD - Implementations decide HOW
class LaserPrinter implements Printer {
    public void print(String text) {
        System.out.println("Laser: " + text);
    }
}

class InkjetPrinter implements Printer {
    public void print(String text) {
        System.out.println("Inkjet: " + text);
    }
}

// ✅ GOOD - Code depends on abstraction, not concrete class
static void sendToPrinter(Printer p) {
    p.print("Hello");  // Works with ANY Printer
}
```

**When to use:** Interface for contracts, abstract class for shared implementation with some abstract methods .

---

## 2. SOLID Principles

SOLID is **foundational for maintainable enterprise Java applications**. Violations lead to fragile, tightly coupled code .

### 2.1 S — Single Responsibility (SRP)

> **"A class should have only one reason to change."**

```java
// ❌ BAD - Multiple responsibilities (business logic + persistence)
class Employee {
    void calculateSalary() { /* business */ }
    void saveToDatabase() { /* persistence */ }  // Violates SRP
}

// ✅ GOOD - Split into two classes
class Employee {
    // ONLY business logic
    void calculateSalary() { ... }
}

class EmployeeRepository {
    // ONLY persistence
    void save(Employee employee) { ... }
}
```

### 2.2 O — Open/Closed (OCP)

> **"Open for extension, closed for modification."**

```java
// ✅ GOOD - Add new payment types without modifying existing code
interface Payment {
    void pay(double amount);
}

class CreditCardPayment implements Payment {
    public void pay(double amount) { /* credit card */ }
}

class PayPalPayment implements Payment {
    public void pay(double amount) { /* PayPal */ }
}

// Can add BitcoinPayment WITHOUT touching existing code 
class BitcoinPayment implements Payment {
    public void pay(double amount) { /* Bitcoin */ }
}
```

### 2.3 L — Liskov Substitution (LSP)

> **"Subtypes must be substitutable for base types without breaking behavior."**

```java
// ❌ BAD - Violates LSP
class Bird {
    void fly() { /* ... */ }
}

class Penguin extends Bird {
    @Override void fly() {
        throw new UnsupportedOperationException();  // Can't fly!
    }
}

// ✅ GOOD - Use interfaces for optional abilities
interface Flyable {
    void fly();
}

class Sparrow implements Flyable {
    public void fly() { /* ... */ }
}

class Penguin extends Bird {
    // Penguin doesn't implement Flyable - it CAN'T fly
}
```

### 2.4 I — Interface Segregation (ISP)

> **"No client should be forced to implement methods it doesn't use."**

```java
// ❌ BAD - Fat interface
interface Worker {
    void work();
    void eat();  // Robot doesn't eat!
}

// ✅ GOOD - Segregate into small interfaces
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

class Robot implements Workable {
    public void work() { /* ... */ }  // No eat() method!
}
```

### 2.5 D — Dependency Inversion (DIP)

> **"Depend on abstractions, not concretions."**

```java
// ❌ BAD - High-level module depends on low-level details
class Notification {
    EmailService emailService;  // Tight coupling!
    void notify(String msg) { emailService.sendEmail(msg); }
}

// ✅ GOOD - Both depend on abstraction
interface MessageService {
    void sendMessage(String message);
}

class EmailService implements MessageService {
    public void sendMessage(String message) { /* ... */ }
}

class Notification {
    private final MessageService service;  // Dependency injection

    public Notification(MessageService service) {
        this.service = service;  // Injected via constructor
    }

    void notify(String msg) {
        service.sendMessage(msg);  // Works with email, SMS, push...
    }
}
```

**DIP in Spring Boot:** Use constructor injection (preferred), not field injection.

---

## 3. Spring Boot Modulith Context

### Apply OOP at MODULE level, not just class level

| OOP Principle | Module Application |
|---------------|-------------------|
| **Encapsulation** | Modules expose only `@NamedInterface` APIs; `internal` packages hidden |
| **Abstraction** | Use interfaces for cross-module communication; publish events |
| **Dependency Inversion** | Modules depend on abstractions (APIs/events) not concrete implementations |

```java
// ✅ GOOD - Module API as abstraction
@NamedInterface("order")
public interface OrderApi {
    OrderDto getOrder(String id);
}

// ✅ GOOD - Depend on API, not implementation
@ApplicationModule(allowedDependencies = {"order"})
public class PaymentModule { /* ... */ }
```

**SOLID in Modulith context:**
- **SRP**: Each module has exactly one bounded context/responsibility
- **LSP**: Module interfaces are substitutable
- **ISP**: Keep APIs focused, don't expose unnecessary methods

---

## 4. Java 25 OOP Features

**Latest certification scope for Java 25 OCP** :

| Feature | Status | Usage |
|---------|--------|-------|
| **Records** | Stable | Immutable data carriers (DTOs) |
| **Flexible Constructor Bodies** | Stable | Validate before `super()` call  |
| **Sealed Classes** | Stable | Restricted inheritance hierarchies |
| **Pattern Matching** | Stable | Clean `instanceof` + switch |
| **Local Variable Type Inference (`var`)** | Stable | Use for local variables  |
| **Unnamed Variables** | Stable | `_` for unused parameters  |

```java
// ✅ GOOD - Sealed class for controlled inheritance
sealed interface Shape permits Circle, Rectangle, Triangle {}

record Circle(double radius) implements Shape {}
record Rectangle(double length, double width) implements Shape {}
record Triangle(double base, double height) implements Shape {}

// ✅ GOOD - Pattern matching with sealed types
double area(Shape shape) {
    return switch (shape) {
        case Circle c -> Math.PI * c.radius() * c.radius();
        case Rectangle r -> r.length() * r.width();
        case Triangle t -> 0.5 * t.base() * t.height();
    };  // Compiler knows all cases covered!
}
```

---

## 5. Coding Standards Quick Reference

| Element | Convention | Example |
|---------|-----------|---------|
| Classes/Interfaces | PascalCase | `OrderService`, `PaymentProcessor` |
| Methods | camelCase (verb phrase) | `getOrder()`, `calculateTotal()` |
| Fields/Local vars | camelCase (noun phrase) | `orderId`, `totalAmount` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`, `DEFAULT_TIMEOUT`  |
| Package | lowercase, reversed domain | `com.company.ordermodule` |
| Boolean methods | `is`/`has` prefix | `isValid()`, `hasNext()`  |
| Comments | `//` for inline, `/* */` for headers | Prefer `//` for commenting out code  |

**Readability rules** :
- Indent 4 spaces per level
- Keep lines under 80 characters
- Surround binary operators with spaces: `int total = a + b;`
- Put space after keywords: `if (condition) {`

---

## Forbidden Behaviors

- ❌ **Public mutable fields** — always private with getters/setters
- ❌ **Field injection** (`@Autowired` on fields) — use constructor injection
- ❌ **Deep inheritance hierarchies** (>3 levels) — prefer composition
- ❌ **Fat interfaces** — split into role-specific interfaces
- ❌ **Depend on concrete classes** in high-level modules — use interfaces
- ❌ **Violate LSP** — don't throw exceptions in overridden methods for "impossible" cases
```