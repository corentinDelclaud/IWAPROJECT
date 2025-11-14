package com.iwaproject.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwaproject.product.dto.CreateProductRequest;
import com.iwaproject.product.dto.ProductDTO;
import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.ServiceType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ProductControllerIntegrationTest {

    private static final String API_PRODUCTS_PATH = "/api/products";

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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllProducts() throws Exception {
        mockMvc.perform(get(API_PRODUCTS_PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testCreateProduct() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                Game.VALORANT,
                ServiceType.COACHING,
                "Test coaching service",
                50.0f,
                false,
                true,
                1
        );

        mockMvc.perform(post(API_PRODUCTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.game").value("VALORANT"))
                .andExpect(jsonPath("$.serviceType").value("COACHING"))
                .andExpect(jsonPath("$.price").value(50.0));
    }

    @Test
    void testGetProductByIdNotFound() throws Exception {
        mockMvc.perform(get(API_PRODUCTS_PATH + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateProduct() throws Exception {
        CreateProductRequest createRequest = new CreateProductRequest(
                Game.LEAGUE_OF_LEGENDS,
                ServiceType.BOOST,  // BOOST au lieu de BOOSTING
                "Boosting service",
                75.0f,
                false,
                true,
                1
        );

        String response = mockMvc.perform(post(API_PRODUCTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ProductDTO createdProduct = objectMapper.readValue(response, ProductDTO.class);

        CreateProductRequest updateRequest = new CreateProductRequest(
                Game.LEAGUE_OF_LEGENDS,
                ServiceType.BOOST,
                "Updated boosting service",
                100.0f,
                false,
                true,
                1
        );

        mockMvc.perform(put(API_PRODUCTS_PATH + "/" + createdProduct.getIdService())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated boosting service"))
                .andExpect(jsonPath("$.price").value(100.0));
    }

    @Test
    void testDeleteProduct() throws Exception {
        CreateProductRequest createRequest = new CreateProductRequest(
                Game.ROCKET_LEAGUE,  // ROCKET_LEAGUE au lieu de OVERWATCH
                ServiceType.COACHING,
                "Rocket League coaching",
                60.0f,
                false,
                true,
                1
        );

        String response = mockMvc.perform(post(API_PRODUCTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ProductDTO createdProduct = objectMapper.readValue(response, ProductDTO.class);

        mockMvc.perform(delete(API_PRODUCTS_PATH + "/" + createdProduct.getIdService()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_PRODUCTS_PATH + "/" + createdProduct.getIdService()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testFindByGame() throws Exception {
        mockMvc.perform(get(API_PRODUCTS_PATH + "/game/VALORANT"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testFindByServiceType() throws Exception {
        mockMvc.perform(get(API_PRODUCTS_PATH + "/type/COACHING"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testToggleAvailability() throws Exception {
        CreateProductRequest createRequest = new CreateProductRequest(
                Game.VALORANT,
                ServiceType.COACHING,
                "Toggle test",
                50.0f,
                false,
                true,
                1
        );

        String response = mockMvc.perform(post(API_PRODUCTS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ProductDTO createdProduct = objectMapper.readValue(response, ProductDTO.class);

        mockMvc.perform(patch(API_PRODUCTS_PATH + "/" + createdProduct.getIdService() + "/toggle-availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(false));
    }

    @Test
    void testSearchProducts() throws Exception {
        mockMvc.perform(get(API_PRODUCTS_PATH + "/search")
                        .param("game", "VALORANT")
                        .param("type", "COACHING")
                        .param("minPrice", "0")
                        .param("maxPrice", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}