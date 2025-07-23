package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String productKey;

    @Column(nullable = false)
    private String brandName;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String imageUrl;

    private String linkUrl;

    // private Coordi coordi;

    private Sex sex;

}