<!-- 48997a12-6025-46e7-ac9a-d41d65125345 448f63cc-85d6-42ef-b0c4-6932596a191e -->
# Spring Boot Test Microservice - Complete Setup

## Current State

You have a fresh Spring Initializr project at `back/test-service/` with:

- Spring Boot 3.5.6
- Java 21
- Dependencies: Spring Web, JPA, H2, PostgreSQL, Lombok
- Package: `com.iwaproject.test_service`
- Main class: `TestServiceApplication.java`

## Goal

Create a working REST API microservice with full CRUD operations for a Product entity, using in-memory storage (no database setup required).

## Implementation Steps

### 1. Disable JPA Auto-Configuration

**File:** `back/test-service/src/main/resources/application.properties`

Since we're not using a database yet, we need to disable JPA:

```properties
spring.application.name=test-service
server.port=8081

# Disable JPA/DataSource auto-configuration (no database for now)
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
```

### 2. Create Product Model

**File:** `back/test-service/src/main/java/com/iwaproject/test_service/model/Product.java`

Create a simple POJO with Lombok annotations:

```java
package com.iwaproject.test_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String description;
    private Double price;
}
```

### 3. Create Product Service

**File:** `back/test-service/src/main/java/com/iwaproject/test_service/service/ProductService.java`

Implement business logic with in-memory storage:

```java
package com.iwaproject.test_service.service;

import com.iwaproject.test_service.model.Product;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {
    private final Map<Long, Product> products = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    // Create
    // Get all
    // Get by ID
    // Update
    // Delete
}
```

### 4. Create REST Controller

**File:** `back/test-service/src/main/java/com/iwaproject/test_service/controller/ProductController.java`

Implement REST endpoints:

```java
package com.iwaproject.test_service.controller;

import com.iwaproject.test_service.model.Product;
import com.iwaproject.test_service.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    
    // Constructor injection
    // POST /api/products
    // GET /api/products
    // GET /api/products/{id}
    // PUT /api/products/{id}
    // DELETE /api/products/{id}
}
```

### 5. Build and Run

Navigate to `back/test-service/` and run:

```bash
mvn clean install
mvn spring-boot:run
```

Service will start on `http://localhost:8081`

### 6. Test with Postman

**A. Create Product (POST)**

- URL: `http://localhost:8081/api/products`
- Method: POST
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 999.99
}
```


Expected: Status 201, returns created product with ID

**B. Get All Products (GET)**

- URL: `http://localhost:8081/api/products`
- Method: GET

Expected: Status 200, returns array of all products

**C. Get Product by ID (GET)**

- URL: `http://localhost:8081/api/products/1`
- Method: GET

Expected: Status 200, returns product with ID 1

**D. Update Product (PUT)**

- URL: `http://localhost:8081/api/products/1`
- Method: PUT
- Headers: `Content-Type: application/json`
- Body (raw JSON):
```json
{
  "name": "Updated Laptop",
  "description": "Gaming laptop with RTX 4090",
  "price": 1499.99
}
```


Expected: Status 200, returns updated product

**E. Delete Product (DELETE)**

- URL: `http://localhost:8081/api/products/1`
- Method: DELETE

Expected: Status 204 (No Content)

**F. Test Error Cases**

- GET `/api/products/999` → Should return 404 Not Found
- DELETE `/api/products/999` → Should return 404 Not Found

## Key Points

- Using Lombok for less boilerplate (no manual getters/setters needed)
- In-memory ConcurrentHashMap for thread-safe storage
- AtomicLong for auto-incrementing IDs
- ResponseEntity for proper HTTP status codes
- No database configuration needed (disabled auto-config)

## Expected File Structure After Implementation

```
back/test-service/src/main/java/com/iwaproject/test_service/
├── TestServiceApplication.java (already exists)
├── controller/
│   └── ProductController.java (NEW)
├── model/
│   └── Product.java (NEW)
└── service/
    └── ProductService.java (NEW)
```

## Success Criteria

- Service starts without errors on port 8081
- All 5 CRUD endpoints working in Postman
- Proper HTTP status codes (201, 200, 204, 404)
- Products persist in memory during runtime
- Clean JSON responses

### To-dos

- [ ] Create product-service directory structure and Maven configuration
- [ ] Create Product entity model class
- [ ] Implement ProductService with in-memory storage
- [ ] Create REST controller with all CRUD endpoints
- [ ] Create ProductServiceApplication main class
- [ ] Configure application.yml with port and settings
- [ ] Build, run, and test all endpoints with Postman