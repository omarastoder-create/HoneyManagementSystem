package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.SellerRequest;
import Rastoder.HoneyManagementSystem.dto.response.SellerResponse;
import Rastoder.HoneyManagementSystem.model.Seller;
import Rastoder.HoneyManagementSystem.repository.SellerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private SellerService sellerService;

    @Test
    @DisplayName("createSeller: When name is unique, should save and return SellerResponse")
    void createSeller_WhenUnique_ShouldSaveAndReturnResponse() {
        // Given
        var request = new SellerRequest("Imkerei Rastoder");
        UUID sellerId = UUID.randomUUID();

        when(sellerRepository.findByName("Imkerei Rastoder")).thenReturn(Optional.empty());

        var savedSeller = Seller.builder()
                .sellerId(sellerId)
                .name("Imkerei Rastoder")
                .build();

        when(sellerRepository.save(any(Seller.class))).thenReturn(savedSeller);

        // When
        SellerResponse response = sellerService.createSeller(request);

        // Then
        verify(sellerRepository).findByName("Imkerei Rastoder");
        verify(sellerRepository).save(any(Seller.class));

        assertThat(response).isNotNull();
        assertThat(response.sellerId()).isEqualTo(sellerId);
        assertThat(response.name()).isEqualTo("Imkerei Rastoder");
    }

    @Test
    @DisplayName("createSeller: When name already exists, should throw IllegalArgumentException")
    void createSeller_WhenNameExists_ShouldThrowException() {
        // Given
        var request = new SellerRequest("Imkerei Rastoder");
        var existingSeller = Seller.builder().sellerId(UUID.randomUUID()).name("Imkerei Rastoder").build();

        when(sellerRepository.findByName("Imkerei Rastoder")).thenReturn(Optional.of(existingSeller));

        // When / Then
        assertThatThrownBy(() -> sellerService.createSeller(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Seller with this name already exists");

        verify(sellerRepository).findByName("Imkerei Rastoder");
        verify(sellerRepository, never()).save(any());
    }

    @Test
    @DisplayName("getAllSellers: Should return list of SellerResponse")
    void getAllSellers_ShouldReturnSellersList() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        var seller1 = Seller.builder().sellerId(id1).name("Imkerei A").build();
        var seller2 = Seller.builder().sellerId(id2).name("Imkerei B").build();

        when(sellerRepository.findAll()).thenReturn(List.of(seller1, seller2));

        // When
        List<SellerResponse> result = sellerService.getAllSellers();

        // Then
        verify(sellerRepository).findAll();
        assertThat(result)
                .hasSize(2)
                .containsExactly(
                        new SellerResponse(id1, "Imkerei A"),
                        new SellerResponse(id2, "Imkerei B")
                );
    }
}