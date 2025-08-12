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
public class Products {
    @Id
    @Column(name = "product_id")
    private Long productId;

    // 예: 상의 / 아우터 / 하의 / 신발
    @Column(name = "category_id", nullable = false)
    private String categoryId;

    // 남성 0, 여성 1
    @Column(name = "gender", nullable = false)
    private Integer gender;

    @Column(name = "brand_name", nullable = false)
    private String brandName;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "link_url")
    private String linkUrl;
}
