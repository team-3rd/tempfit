package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cloth_female")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothFemale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clothId;

    private String clothName;

    private String category;  // 'outer', 'top', 'bottom', 'shoes'

    private String imageUrl;
}
