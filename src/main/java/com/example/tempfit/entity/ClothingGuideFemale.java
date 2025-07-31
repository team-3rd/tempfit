package com.example.tempfit.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clothing_guide_female")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothingGuideFemale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloth_id")
    private ClothFemale clothFemale;

    private int tempRange;
}
