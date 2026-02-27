package com.barber.barberapp.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "services")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nameEn;
    private String nameAr;
    private String nameHe;        // Arabic name
    private double price;         // e.g. 50
    private Integer durationMinutes;  // 30, 60
    @Column(nullable = false)
    private boolean active = true;
}
