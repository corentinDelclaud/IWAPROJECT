package com.iwaproject.product.service;

import com.iwaproject.product.dto.ProductDTO;
import com.iwaproject.product.dto.CreateProductRequest;
import com.iwaproject.product.model.Game;
import com.iwaproject.product.model.ServiceType;
import com.iwaproject.product.model.Product;
import com.iwaproject.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private CreateProductRequest testCreateRequest;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setIdService(1);
        testProduct.setGame(Game.VALORANT);
        testProduct.setServiceType(ServiceType.COACHING);
        testProduct.setDescription("Test coaching");
        testProduct.setPrice(50.0f);
        testProduct.setUnique(false);
        testProduct.setIsAvailable(true);
        testProduct.setIdProvider(1);

        testCreateRequest = new CreateProductRequest(
                Game.VALORANT,
                ServiceType.COACHING,
                "Test coaching",
                50.0f,
                false,
                true,
                1
        );
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(testProduct));

        List<ProductDTO> result = productService.getAllProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductByIdFound() {
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));

        Optional<ProductDTO> result = productService.getProductById(1);

        assertTrue(result.isPresent());
        verify(productRepository, times(1)).findById(1);
    }

    @Test
    void testGetProductByIdNotFound() {
        when(productRepository.findById(999)).thenReturn(Optional.empty());

        Optional<ProductDTO> result = productService.getProductById(999);

        assertFalse(result.isPresent());
        verify(productRepository, times(1)).findById(999);
    }

    @Test
    void testCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO result = productService.createProduct(testCreateRequest);

        assertNotNull(result);
        assertEquals(50.0f, result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testUpdateProductSuccess() {
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        CreateProductRequest updateRequest = new CreateProductRequest(
                Game.VALORANT,
                ServiceType.COACHING,
                "Updated description",
                75.0f,
                false,
                true,
                1
        );

        Optional<ProductDTO> result = productService.updateProduct(1, updateRequest);

        assertTrue(result.isPresent());
        verify(productRepository, times(1)).findById(1);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        when(productRepository.existsById(1)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1);

        boolean result = productService.deleteProduct(1);

        assertTrue(result);
        verify(productRepository, times(1)).existsById(1);
        verify(productRepository, times(1)).deleteById(1);
    }

    @Test
    void testFindByGame() {
        when(productRepository.findByGame(Game.VALORANT.name())).thenReturn(Arrays.asList(testProduct));

        List<ProductDTO> result = productService.getProductsByGame(Game.VALORANT);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByGame(Game.VALORANT.name());
    }

    @Test
    void testFindByServiceType() {
        when(productRepository.findByServiceType(ServiceType.COACHING.name()))
                .thenReturn(Arrays.asList(testProduct));

        List<ProductDTO> result = productService.getProductsByType(ServiceType.COACHING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByServiceType(ServiceType.COACHING.name());
    }

    @Test
    void testFindByIdProvider() {
        when(productRepository.findByIdProvider(1)).thenReturn(Arrays.asList(testProduct));

        List<ProductDTO> result = productService.getProductsByProvider(1);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getIdProvider());
        verify(productRepository, times(1)).findByIdProvider(1);
    }

    @Test
    void testGetAvailableProducts() {
        when(productRepository.findByIsAvailableTrue()).thenReturn(Arrays.asList(testProduct));

        List<ProductDTO> result = productService.getAvailableProducts();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsAvailable());
        verify(productRepository, times(1)).findByIsAvailableTrue();
    }

    @Test
    void testToggleAvailability() {
        when(productRepository.findById(1)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Optional<ProductDTO> result = productService.toggleAvailability(1);

        assertTrue(result.isPresent());
        verify(productRepository, times(1)).findById(1);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testGetProductsByGameAndType() {
        when(productRepository.findByGameAndServiceType(Game.VALORANT.name(), ServiceType.COACHING.name()))
                .thenReturn(Arrays.asList(testProduct));

        List<ProductDTO> result = productService.getProductsByGameAndType(Game.VALORANT, ServiceType.COACHING);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("COACHING", result.get(0).getServiceType());
        verify(productRepository, times(1)).findByGameAndServiceType(Game.VALORANT.name(), ServiceType.COACHING.name());
    }

    @Test
    void testGetProductsByFilters() {
        when(productRepository.findByFilters(
                Game.VALORANT.name(),
                ServiceType.COACHING.name(),
                0.0f,
                100.0f,
                1
        )).thenReturn(Arrays.asList(testProduct));

        List<ProductDTO> result = productService.getProductsByFilters(
                Game.VALORANT,
                ServiceType.COACHING,
                0.0f,
                100.0f,
                1
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByFilters(
                Game.VALORANT.name(),
                ServiceType.COACHING.name(),
                0.0f,
                100.0f,
                1
        );
    }
}