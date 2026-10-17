package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.ProductRequest;
import Rastoder.HoneyManagementSystem.dto.response.ProductResponse;
import Rastoder.HoneyManagementSystem.model.Product;
import Rastoder.HoneyManagementSystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProductResponse createProduct(ProductRequest request, MultipartFile image) {
        // Lagert die Speicherung aus und holt den relativen Pfad
        String imageUrl = fileStorageService.saveImage(image);

        Product product = Product.builder()
                .description(request.description())
                .pricePerUnit(request.pricePerUnit())
                .unitsToSell(request.unitsToSell())
                .dateOfHarvest(request.dateOfHarvest())
                .imageUrl(imageUrl) // Pfad an die Entity übergeben
                .build();

        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::mapToProductResponse)
                .toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return new ProductResponse(
                product.getProductId(),
                product.getDescription(),
                product.getPricePerUnit(),
                product.getUnitsToSell(),
                product.getDateOfHarvest(),
                product.getImageUrl() // Bild-URL ins DTO mappen
        );
    }
}