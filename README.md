# Honey Management System

A robust enterprise-grade Spring Boot application designed for managing honey production, inventory batches, and sales transactions. Built following modern software architecture standards.

## Tech Stack
* **Language:** Java 21
* **Framework:** Spring Boot
* **Build Tool:** Maven (Maven Wrapper included)
* **CI/CD & Quality:** GitHub Actions, SonarCloud, JaCoCo Code Coverage

## Architecture & Design Principles
* **Strict Layered Separation:** Controllers handle HTTP routing, Services encapsulate business logic, and Repositories manage data persistence.
* **Modern Java Features:** Leverages Records, Pattern Matching, and the Streams API for immutable and expressive data handling.
* **Dependency Injection:** Strictly enforces Constructor Injection, completely avoiding field injection (`@Autowired`).
* **DTO Isolation:** Request/Response payloads are mapped to dedicated Data Transfer Objects to keep domain entities safe from the presentation layer.
