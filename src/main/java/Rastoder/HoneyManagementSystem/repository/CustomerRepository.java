package Rastoder.HoneyManagementSystem.repository;

import Rastoder.HoneyManagementSystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    List<Customer> findByNameContainingIgnoreCaseOrFamilyNameContainingIgnoreCase(String name, String familyName);
}