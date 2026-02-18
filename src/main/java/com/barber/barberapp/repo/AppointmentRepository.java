package com.barber.barberapp.repo;

import com.barber.barberapp.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);
    List<Appointment> findAllByOrderByStartTimeAsc();
    long countByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Appointment> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime start, LocalDateTime end);

    boolean existsByStartTimeLessThanAndEndTimeGreaterThan(LocalDateTime end, LocalDateTime start);


    @Query("""
       SELECT a.service.name, COUNT(a)
       FROM Appointment a
       WHERE a.startTime BETWEEN :start AND :end
       GROUP BY a.service.name
       ORDER BY COUNT(a) DESC
       """)
    List<Object[]> findServiceStats(LocalDateTime start, LocalDateTime end);

}
