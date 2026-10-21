package Rastoder.HoneyManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "customers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "customer_id", nullable = false, updatable = false, unique = true)
    private UUID customerId;

    @Column(nullable = false)
    private String name;

    @Column(name = "family_name", nullable = false)
    private String familyName;

    private String description;

    @Column(unique = true)
    private String phoneNumber;
}