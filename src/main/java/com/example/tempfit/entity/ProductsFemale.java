package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products_female")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductsFemale {
    // 외부 Musinsa 상품 고유 아이디를 PK로 사용
    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "category_id")
    private String categoryId;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "link_url")
    private String linkUrl;
}
