package com.iwaproject.product.e2e;

import com.iwaproject.product.dto.CreateProductRequest;
import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.ServiceType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductE2ETest {

    private static final String SIZE = "size()";
    private static final String ID_SERVICE = "idService";


    private static final String API_PRODUCTS_PATH = "/api/products";

    @LocalServerPort
    private Integer port;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }

    @Test
    void testCompleteProductLifecycle() {
        // Create a product
        CreateProductRequest createRequest = new CreateProductRequest(
                Game.VALORANT,
                ServiceType.COACHING,
                "E2E Test Coaching",
                85.0f,
                false,
                true,
                1
        );

        Integer productId = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post(API_PRODUCTS_PATH)
                .then()
                .statusCode(201)
                .body("game", equalTo("Valorant"))
                .body("serviceType", equalTo("COACHING"))
                .body("price", equalTo(85.0f))
                .extract()
                .path(ID_SERVICE);

        // Get the product by ID
        given()
                .when()
                .get(API_PRODUCTS_PATH + "/" + productId)
                .then()
                .statusCode(200)
                .body(ID_SERVICE, equalTo(productId))
                .body("description", equalTo("E2E Test Coaching"));

        // Update the product
        CreateProductRequest updateRequest = new CreateProductRequest(
                Game.VALORANT,
                ServiceType.COACHING,
                "Updated E2E Test Coaching",
                95.0f,
                false,
                true,
                1
        );

        given()
                .contentType(ContentType.JSON)
                .body(updateRequest)
                .when()
                .put(API_PRODUCTS_PATH + "/" + productId)
                .then()
                .statusCode(200)
                .body("description", equalTo("Updated E2E Test Coaching"))
                .body("price", equalTo(95.0f));

        // Get all products
        given()
                .when()
                .get(API_PRODUCTS_PATH)
                .then()
                .statusCode(200)
                .body(SIZE, greaterThan(0));

        // Toggle availability
        given()
                .when()
                .patch(API_PRODUCTS_PATH + "/" + productId + "/toggle-availability")
                .then()
                .statusCode(200)
                .body("isAvailable", equalTo(false));

        // Search by game
        given()
                .when()
                .get(API_PRODUCTS_PATH + "/game/VALORANT")
                .then()
                .statusCode(200)
                .body(SIZE, greaterThan(0));

        // Search by type
        given()
                .when()
                .get(API_PRODUCTS_PATH + "/type/COACHING")
                .then()
                .statusCode(200)
                .body(SIZE, greaterThan(0));

        // Search with filters
        given()
                .queryParam("game", "VALORANT")
                .queryParam("type", "COACHING")
                .queryParam("minPrice", "0")
                .queryParam("maxPrice", "100")
                .when()
                .get(API_PRODUCTS_PATH + "/search")
                .then()
                .statusCode(200)
                .body(SIZE, greaterThan(0));

        // Delete the product
        given()
                .when()
                .delete(API_PRODUCTS_PATH + "/" + productId)
                .then()
                .statusCode(204);

        // Verify deletion
        given()
                .when()
                .get(API_PRODUCTS_PATH + "/" + productId)
                .then()
                .statusCode(404);
    }

    @Test
    void testGetProductsByProvider() {
        // Create a product
        CreateProductRequest createRequest = new CreateProductRequest(
                Game.LEAGUE_OF_LEGENDS,
                ServiceType.BOOST,
                "Provider Test",
                50.0f,
                false,
                true,
                1
        );

        Integer productId = given()
                .contentType(ContentType.JSON)
                .body(createRequest)
                .when()
                .post(API_PRODUCTS_PATH)
                .then()
                .statusCode(201)
                .extract()
                .path(ID_SERVICE);

        // Get products by provider
        given()
                .when()
                .get(API_PRODUCTS_PATH + "/provider/1")
                .then()
                .statusCode(200)
                .body(SIZE, greaterThan(0))
                .body("[0].idProvider", equalTo(1));

        // Cleanup
        given()
                .when()
                .delete(API_PRODUCTS_PATH + "/" + productId)
                .then()
                .statusCode(204);
    }

    @Test
    void testGetAvailableProducts() {
        given()
                .queryParam("available", true)
                .when()
                .get(API_PRODUCTS_PATH)
                .then()
                .statusCode(200);
    }
}