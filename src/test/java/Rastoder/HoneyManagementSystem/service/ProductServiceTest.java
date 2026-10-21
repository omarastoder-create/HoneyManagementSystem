package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.ProductRequest;
import Rastoder.HoneyManagementSystem.dto.response.ProductResponse;
import Rastoder.HoneyManagementSystem.model.Product;
import Rastoder.HoneyManagementSystem.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("createProduct: Should save image, persist product, and return ProductResponse")
    void createProduct_ShouldSaveImageAndProduct() {
        // Given
        var request = new ProductRequest("Blütenhonig", new BigDecimal("12.50"), 50, LocalDate.now(), null);
        var image = new MockMultipartFile("image", "honey.png", "image/png", new byte[]{1, 2, 3});

        UUID productId = UUID.randomUUID();
        String savedImagePath = "/images/uuid_honey.png";

        when(fileStorageService.saveImage(image)).thenReturn(savedImagePath);

        var savedProduct = Product.builder()
                .productId(productId)
                .description(request.description())
                .pricePerUnit(request.pricePerUnit())
                .unitsToSell(request.unitsToSell())
                .dateOfHarvest(request.dateOfHarvest())
                .imageUrl(savedImagePath)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductResponse response = productService.createProduct(request, image);

        // Then
        verify(fileStorageService).saveImage(image);
        verify(productRepository).save(any(Product.class));

        assertThat(response).isNotNull();
        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.description()).isEqualTo("Blütenhonig");
        assertThat(response.pricePerUnit()).isEqualTo(new BigDecimal("12.50"));
        assertThat(response.unitsToSell()).isEqualTo(50);
        assertThat(response.imageUrl()).isEqualTo(savedImagePath);
    }

    @Test
    @DisplayName("createProduct: Without image, should persist product without storing a file")
    void createProduct_WithoutImage_ShouldNotStoreFile() {
        // Given
        var request = new ProductRequest("Waldhonig", new BigDecimal("15.00"), 10, LocalDate.now(), null);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ProductResponse response = productService.createProduct(request, null);

        // Then
        verify(fileStorageService, never()).saveImage(any());
        assertThat(response.imageUrl()).isNull();
        assertThat(response.description()).isEqualTo("Waldhonig");
    }

    @Test
    @DisplayName("getAllProducts: Should return list of ProductResponse")
    void getAllProducts_ShouldReturnProductsList() {
        // Given
        UUID productId = UUID.randomUUID();
        var product = Product.builder()
                .productId(productId)
                .description("Waldhonig")
                .pricePerUnit(new BigDecimal("15.00"))
                .unitsToSell(20)
                .dateOfHarvest(LocalDate.now())
                .imageUrl("/images/wald.png")
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product));

        // When
        List<ProductResponse> result = productService.getAllProducts();

        // Then
        verify(productRepository).findAll();
        assertThat(result)
                .hasSize(1)
                .containsExactly(new ProductResponse(
                        productId,
                        "Waldhonig",
                        new BigDecimal("15.00"),
                        20,
                        product.getDateOfHarvest(),
                        "/images/wald.png"
                ));
    }
}