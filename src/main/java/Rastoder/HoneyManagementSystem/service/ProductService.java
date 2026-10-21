package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.ProductRequest;
import Rastoder.HoneyManagementSystem.dto.response.ProductResponse;
import Rastoder.HoneyManagementSystem.model.Product;
import Rastoder.HoneyManagementSystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProductResponse createProduct(ProductRequest request, MultipartFile image) {
        // Das Bild ist optional
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.saveImage(image);
            deleteImageOnRollback(imageUrl);
        }

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

    // The file is written to disk immediately, but the product row is only
    // committed at the end of the transaction. If that commit fails, remove the
    // file again so no orphaned images pile up in the uploads folder.
    private void deleteImageOnRollback(String imageUrl) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    fileStorageService.deleteImage(imageUrl);
                }
            }
        });
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