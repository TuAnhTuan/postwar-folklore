package com.postwarfolklore.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** URL-friendly identifier, e.g. "quang-nam", "da-nang", "hanoi" */
    @Column(unique = true, nullable = false, length = 50)
    private String slug;

    /** Display name, e.g. "Quảng Nam", "Đà Nẵng", "Hà Nội" */
    @Column(nullable = false, length = 100)
    private String name;

    /** Broad region, e.g. "Miền Trung", "Miền Bắc", "Miền Nam" */
    @Column(length = 100)
    private String region;

    /** ISO 3166-1 alpha-2 country code, default VN */
    @Column(length = 10)
    @Builder.Default
    private String country = "VN";
}
