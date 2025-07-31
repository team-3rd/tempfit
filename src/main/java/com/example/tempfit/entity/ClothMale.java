package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cloth_male")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothMale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clothId;

    private String clothName;

    private String category;  // 'outer', 'top', 'bottom', 'shoes'

    private String imageUrl;
}
