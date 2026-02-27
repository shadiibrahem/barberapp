package com.barber.barberapp.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "working_hours")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class WorkingHours {

    @Id
    private Long id = 1L; // always 1 row

    private LocalTime openTime = LocalTime.of(10, 0);
    private LocalTime closeTime = LocalTime.of(19, 0);

    private Integer slotStepMinutes = 5; // 5 minutes step
}