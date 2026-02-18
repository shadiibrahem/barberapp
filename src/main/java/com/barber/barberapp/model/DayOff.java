package com.barber.barberapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "days_off", uniqueConstraints = @UniqueConstraint(columnNames = "off_date"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class DayOff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "off_date", nullable = false)
    private LocalDate offDate;

    private String reason; // optional
}
