package com.barber.barberapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;
    private String phone;

    @ManyToOne
    private Service service;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status; // BOOKED
}
