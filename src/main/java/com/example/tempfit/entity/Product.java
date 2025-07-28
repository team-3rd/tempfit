package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    /**
     * 외부 Musinsa 상품 고유 아이디를 PK로 사용
     */
    @Id
    @Column(name = "product_id")
    private Long productId;

    /**
     * 검색 키워드 또는 상품 키 (ex: "피케/카라티")
     */
    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private Sex sex;
}
