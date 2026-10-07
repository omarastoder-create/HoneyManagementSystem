package Rastoder.HoneyManagementSystem.dto.response;

public record ProductBestCustomer(
        String productDescription,
        String customerName,
        String customerFamilyName,
        Long totalQuantityBought
) {
}
