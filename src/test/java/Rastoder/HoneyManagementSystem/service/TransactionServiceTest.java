package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.SaleTransactionRequest;
import Rastoder.HoneyManagementSystem.dto.request.TransactionItemRequest;
import Rastoder.HoneyManagementSystem.dto.response.SaleTransactionResponse;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private SaleTransactionRepository saleTransactionRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    @DisplayName("createSaleTransaction: Should process regular price, custom price, and gift items and reduce stock")
    void createSaleTransaction_WithAllPricingBranches_ShouldSaveAndReturnResponse() {
        // Given
        UUID sellerId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID prodRegularId = UUID.randomUUID();
        UUID prodCustomId = UUID.randomUUID();
        UUID prodGiftId = UUID.randomUUID();

        var seller = Seller.builder().sellerId(sellerId).name("Omar").build();
        var customer = Customer.builder().customerId(customerId).name("Max").build();

        var prodRegular = Product.builder()
                .productId(prodRegularId)
                .description("Blütenhonig")
                .pricePerUnit(new BigDecimal("12.00"))
                .unitsToSell(10)
                .build();

        var prodCustom = Product.builder()
                .productId(prodCustomId)
                .description("Waldhonig")
                .pricePerUnit(new BigDecimal("15.00"))
                .unitsToSell(10)
                .build();

        var prodGift = Product.builder()
                .productId(prodGiftId)
                .description("Akazienhonig")
                .pricePerUnit(new BigDecimal("18.00"))
                .unitsToSell(5)
                .build();

        var itemRegular = new TransactionItemRequest(prodRegularId, 2, null, false);
        var itemCustom = new TransactionItemRequest(prodCustomId, 3, new BigDecimal("10.00"), false);
        var itemGift = new TransactionItemRequest(prodGiftId, 1, null, true);

        var request = new SaleTransactionRequest(sellerId, customerId, List.of(itemRegular, itemCustom, itemGift));

        when(sellerRepository.findById(request.sellerId())).thenReturn(Optional.of(seller));
        when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(customer));
        when(productRepository.findById(prodRegularId)).thenReturn(Optional.of(prodRegular));
        when(productRepository.findById(prodCustomId)).thenReturn(Optional.of(prodCustom));
        when(productRepository.findById(prodGiftId)).thenReturn(Optional.of(prodGift));

        when(saleTransactionRepository.save(any(SaleTransaction.class))).thenAnswer(invocation -> {
            SaleTransaction tx = invocation.getArgument(0);
            tx.setTransactionId(UUID.randomUUID());
            return tx;
        });

        // When
        SaleTransactionResponse response = transactionService.createSaleTransaction(request);

        // Then
        verify(saleTransactionRepository).save(any(SaleTransaction.class));

        // Bestandsreduzierung prüfen
        assertThat(prodRegular.getUnitsToSell()).isEqualTo(8);
        assertThat(prodCustom.getUnitsToSell()).isEqualTo(7);
        assertThat(prodGift.getUnitsToSell()).isEqualTo(4);

        // Gesamtpreis: (2 * 12.00) + (3 * 10.00) + (1 * 0.00) = 54.00
        assertThat(response.totalPrice()).isEqualByComparingTo("54.00");
        assertThat(response.sellerName()).isEqualTo("Omar");
        assertThat(response.customerName()).isEqualTo("Max");
        assertThat(response.items()).hasSize(3);

        // Reihenfolgen-unabhängige Prüfung für Set<TransactionItem>
        assertThat(response.items())
                .anySatisfy(item -> {
                    assertThat(item.productId()).isEqualTo(prodRegularId);
                    assertThat(item.pricePerUnit()).isEqualByComparingTo("12.00");
                    assertThat(item.subtotalPrice()).isEqualByComparingTo("24.00");
                    assertThat(item.isGift()).isFalse();
                })
                .anySatisfy(item -> {
                    assertThat(item.productId()).isEqualTo(prodCustomId);
                    assertThat(item.pricePerUnit()).isEqualByComparingTo("10.00");
                    assertThat(item.subtotalPrice()).isEqualByComparingTo("30.00");
                    assertThat(item.isGift()).isFalse();
                })
                .anySatisfy(item -> {
                    assertThat(item.productId()).isEqualTo(prodGiftId);
                    assertThat(item.pricePerUnit()).isEqualByComparingTo("0.00");
                    assertThat(item.subtotalPrice()).isEqualByComparingTo("0.00");
                    assertThat(item.isGift()).isTrue();
                });
    }

    @Test
    @DisplayName("createSaleTransaction: When seller not found, should throw EntityNotFoundException")
    void createSaleTransaction_WhenSellerNotFound_ShouldThrowException() {
        // Given
        var request = new SaleTransactionRequest(UUID.randomUUID(), UUID.randomUUID(), List.of());
        when(sellerRepository.findById(request.sellerId())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.createSaleTransaction(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Seller not found");
    }

    @Test
    @DisplayName("createSaleTransaction: When customer not found, should throw EntityNotFoundException")
    void createSaleTransaction_WhenCustomerNotFound_ShouldThrowException() {
        // Given
        var request = new SaleTransactionRequest(UUID.randomUUID(), UUID.randomUUID(), List.of());
        when(sellerRepository.findById(request.sellerId())).thenReturn(Optional.of(new Seller()));
        when(customerRepository.findById(request.customerId())).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.createSaleTransaction(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Customer not found");
    }

    @Test
    @DisplayName("createSaleTransaction: When product not found, should throw EntityNotFoundException")
    void createSaleTransaction_WhenProductNotFound_ShouldThrowException() {
        // Given
        UUID productId = UUID.randomUUID();
        var itemReq = new TransactionItemRequest(productId, 1, null, false);
        var request = new SaleTransactionRequest(UUID.randomUUID(), UUID.randomUUID(), List.of(itemReq));

        when(sellerRepository.findById(request.sellerId())).thenReturn(Optional.of(new Seller()));
        when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.createSaleTransaction(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Product not found");
    }

    @Test
    @DisplayName("createSaleTransaction: When stock is insufficient, should throw IllegalArgumentException")
    void createSaleTransaction_WhenInsufficientStock_ShouldThrowException() {
        // Given
        UUID productId = UUID.randomUUID();
        var product = Product.builder()
                .productId(productId)
                .description("Sommertracht")
                .unitsToSell(2)
                .build();

        var itemReq = new TransactionItemRequest(productId, 5, null, false);
        var request = new SaleTransactionRequest(UUID.randomUUID(), UUID.randomUUID(), List.of(itemReq));

        when(sellerRepository.findById(request.sellerId())).thenReturn(Optional.of(new Seller()));
        when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When / Then
        assertThatThrownBy(() -> transactionService.createSaleTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nicht genug Bestand für Produkt: Sommertracht");

        verify(saleTransactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("createSaleTransaction: When custom price is negative, should throw IllegalArgumentException")
    void createSaleTransaction_WhenNegativeCustomPrice_ShouldThrowException() {
        // Given
        UUID productId = UUID.randomUUID();
        var product = Product.builder()
                .productId(productId)
                .description("Sommertracht")
                .unitsToSell(10)
                .build();

        var itemReq = new TransactionItemRequest(productId, 1, new BigDecimal("-5.00"), false);
        var request = new SaleTransactionRequest(UUID.randomUUID(), UUID.randomUUID(), List.of(itemReq));

        when(sellerRepository.findById(request.sellerId())).thenReturn(Optional.of(new Seller()));
        when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When / Then
        assertThatThrownBy(() -> transactionService.createSaleTransaction(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Der Preis darf nicht negativ sein.");
    }

    @Test
    @DisplayName("createSaleTransaction: Custom price of 0 without gift flag should not be reported as a gift")
    void createSaleTransaction_WithZeroCustomPrice_ShouldNotBeMarkedAsGift() {
        // Given
        UUID productId = UUID.randomUUID();
        var product = Product.builder()
                .productId(productId)
                .description("Rapshonig")
                .pricePerUnit(new BigDecimal("9.00"))
                .unitsToSell(5)
                .build();

        var itemReq = new TransactionItemRequest(productId, 1, BigDecimal.ZERO, false);
        var request = new SaleTransactionRequest(UUID.randomUUID(), UUID.randomUUID(), List.of(itemReq));

        when(sellerRepository.findById(request.sellerId())).thenReturn(Optional.of(new Seller()));
        when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(saleTransactionRepository.save(any(SaleTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SaleTransactionResponse response = transactionService.createSaleTransaction(request);

        // Then
        var item = response.items().get(0);
        assertThat(item.pricePerUnit()).isEqualByComparingTo("0.00");
        assertThat(item.isGift()).isFalse();
    }

    @Test
    @DisplayName("createSaleTransaction: Unit price is stored exactly, not derived from subtotal / quantity")
    void createSaleTransaction_ShouldStoreExactUnitPrice() {
        // Given
        UUID productId = UUID.randomUUID();
        var product = Product.builder()
                .productId(productId)
                .description("Lindenhonig")
                .pricePerUnit(new BigDecimal("3.335"))
                .unitsToSell(10)
                .build();

        var itemReq = new TransactionItemRequest(productId, 3, null, false);
        var request = new SaleTransactionRequest(UUID.randomUUID(), UUID.randomUUID(), List.of(itemReq));

        when(sellerRepository.findById(request.sellerId())).thenReturn(Optional.of(new Seller()));
        when(customerRepository.findById(request.customerId())).thenReturn(Optional.of(new Customer()));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(saleTransactionRepository.save(any(SaleTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SaleTransactionResponse response = transactionService.createSaleTransaction(request);

        // Then
        assertThat(response.items().get(0).pricePerUnit()).isEqualByComparingTo("3.335");
        assertThat(response.items().get(0).subtotalPrice()).isEqualByComparingTo("10.005");
    }

    @Test
    @DisplayName("getAllTransactions: Should return mapped list with stored unit price and gift flag")
    void getAllTransactions_ShouldReturnMappedTransactions() {
        // Given
        var seller = Seller.builder().name("Omar").build();
        var customer = Customer.builder().name("Kunde A").build();
        var product = Product.builder().productId(UUID.randomUUID()).description("Honig").build();

        var giftItem = TransactionItem.builder()
                .itemId(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .unitPrice(BigDecimal.ZERO)
                .gift(true)
                .subtotalPrice(BigDecimal.ZERO)
                .build();

        var tx = SaleTransaction.builder()
                .transactionId(UUID.randomUUID())
                .saleDate(LocalDateTime.now())
                .seller(seller)
                .customer(customer)
                .totalPrice(BigDecimal.ZERO)
                .items(Set.of(giftItem))
                .build();

        when(saleTransactionRepository.findAllByOrderBySaleDateDesc()).thenReturn(List.of(tx));

        // When
        List<SaleTransactionResponse> result = transactionService.getAllTransactions();

        // Then
        verify(saleTransactionRepository).findAllByOrderBySaleDateDesc();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).items().get(0).pricePerUnit()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.get(0).items().get(0).isGift()).isTrue();
    }

    @Test
    @DisplayName("getTransactionById: When found, should return SaleTransactionResponse")
    void getTransactionById_WhenFound_ShouldReturnResponse() {
        // Given
        var product = Product.builder().productId(UUID.randomUUID()).description("Honig").build();
        var item = TransactionItem.builder()
                .itemId(UUID.randomUUID())
                .product(product)
                .quantity(1)
                .unitPrice(new BigDecimal("25.00"))
                .subtotalPrice(new BigDecimal("25.00"))
                .build();
        UUID txId = UUID.randomUUID();
        var tx = SaleTransaction.builder()
                .transactionId(txId)
                .saleDate(LocalDateTime.now())
                .seller(Seller.builder().name("Omar").build())
                .customer(Customer.builder().name("Kunde").build())
                .totalPrice(new BigDecimal("25.00"))
                .items(Set.of(item))
                .build();

        when(saleTransactionRepository.findWithDetailsByTransactionId(txId)).thenReturn(Optional.of(tx));

        // When
        SaleTransactionResponse response = transactionService.getTransactionById(txId);

        // Then
        assertThat(response.transactionId()).isEqualTo(txId);
        assertThat(response.totalPrice()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("getTransactionById: When not found, should throw EntityNotFoundException")
    void getTransactionById_WhenNotFound_ShouldThrowException() {
        // Given
        UUID txId = UUID.randomUUID();
        when(saleTransactionRepository.findWithDetailsByTransactionId(txId)).thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> transactionService.getTransactionById(txId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Transaction not found");
    }
}