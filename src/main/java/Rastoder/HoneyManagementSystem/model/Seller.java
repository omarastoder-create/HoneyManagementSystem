package Rastoder.HoneyManagementSystem.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "sellers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(nullable = false, unique = true)
    private String name;
}