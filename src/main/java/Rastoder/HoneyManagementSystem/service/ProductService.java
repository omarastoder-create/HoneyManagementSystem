package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.ProductRequest;
import Rastoder.HoneyManagementSystem.dto.response.ProductResponse;
import Rastoder.HoneyManagementSystem.model.Product;
import Rastoder.HoneyManagementSystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = Product.builder()
                .description(request.description())
                .pricePerUnit(request.pricePerUnit())
                .unitsToSell(request.unitsToSell())
                .dateOfHarvest(request.dateOfHarvest())
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
                product.getDateOfHarvest()
        );
    }
}