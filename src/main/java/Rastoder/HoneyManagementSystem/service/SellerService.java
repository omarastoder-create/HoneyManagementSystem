package Rastoder.HoneyManagementSystem.service;

import Rastoder.HoneyManagementSystem.dto.request.SellerRequest;
import Rastoder.HoneyManagementSystem.dto.response.SellerResponse;
import Rastoder.HoneyManagementSystem.model.Seller;
import Rastoder.HoneyManagementSystem.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @Transactional
    public SellerResponse createSeller(SellerRequest request) {
        if (sellerRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException("Seller with this name already exists");
            //Since this is for my family only for the moment , names have to be unique
        }

        Seller seller = Seller.builder()
                .name(request.name())
                .build();

        Seller savedSeller = sellerRepository.save(seller);
        return mapToResponse(savedSeller);
    }

    @Transactional(readOnly = true)
    public List<SellerResponse> getAllSellers() {
        return sellerRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SellerResponse mapToResponse(Seller seller) {
        return new SellerResponse(seller.getSellerId(), seller.getName());
    }
}