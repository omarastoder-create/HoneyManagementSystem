package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.SaleTransactionRequest;
import Rastoder.HoneyManagementSystem.dto.request.TransactionItemRequest;
import Rastoder.HoneyManagementSystem.dto.response.SaleTransactionResponse;
import Rastoder.HoneyManagementSystem.dto.response.TransactionItemResponse;
import Rastoder.HoneyManagementSystem.model.Customer;
import Rastoder.HoneyManagementSystem.model.Product;
import Rastoder.HoneyManagementSystem.model.SaleTransaction;
import Rastoder.HoneyManagementSystem.model.Seller;
import Rastoder.HoneyManagementSystem.model.TransactionItem;
import Rastoder.HoneyManagementSystem.repository.CustomerRepository;
import Rastoder.HoneyManagementSystem.repository.ProductRepository;
import Rastoder.HoneyManagementSystem.repository.SaleTransactionRepository;
import Rastoder.HoneyManagementSystem.repository.SellerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final SaleTransactionRepository saleTransactionRepository;
    private final SellerRepository sellerRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Transactional
    public SaleTransactionResponse createSaleTransaction(SaleTransactionRequest request) {

        Seller seller = sellerRepository.findById(request.sellerId())
                .orElseThrow(() -> new EntityNotFoundException("Seller not found"));

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        SaleTransaction transaction = SaleTransaction.builder()
                .saleDate(LocalDateTime.now())
                .seller(seller)
                .customer(customer)
                .totalPrice(BigDecimal.ZERO)
                .build();

        BigDecimal calculatedTotal = BigDecimal.ZERO;

        for (TransactionItemRequest itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found"));

            if (product.getUnitsToSell() < itemReq.quantity()) {
                throw new IllegalArgumentException("Nicht genug Bestand für Produkt: " + product.getDescription());
            }

            product.reduceStock(itemReq.quantity());

            BigDecimal subtotal = product.getPricePerUnit().multiply(BigDecimal.valueOf(itemReq.quantity()));
            calculatedTotal = calculatedTotal.add(subtotal);

            TransactionItem item = TransactionItem.builder()
                    .transaction(transaction)
                    .product(product)
                    .quantity(itemReq.quantity())
                    .subtotalPrice(subtotal)
                    .build();

            transaction.getItems().add(item);
        }

        transaction.setTotalPrice(calculatedTotal);
        SaleTransaction saved = saleTransactionRepository.save(transaction);

        return mapToResponse(saved);
    }

    private SaleTransactionResponse mapToResponse(SaleTransaction transaction) {
        return new SaleTransactionResponse(
                transaction.getTransactionId(),
                transaction.getSaleDate(),
                transaction.getSeller().getSellerId(),
                transaction.getCustomer().getCustomerId(),
                transaction.getTotalPrice(),
                transaction.getItems().stream()
                        .map(item -> new TransactionItemResponse(
                                item.getItemId(),
                                item.getProduct().getProductId(),
                                item.getQuantity(),
                                item.getSubtotalPrice()
                        )).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<SaleTransactionResponse> getAllTransactions() {
        return saleTransactionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SaleTransactionResponse getTransactionById(UUID id) {
        SaleTransaction transaction = saleTransactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));
        return mapToResponse(transaction);
    }
}