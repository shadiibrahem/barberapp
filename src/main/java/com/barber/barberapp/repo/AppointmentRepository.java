package com.barber.barberapp.repo;

import com.barber.barberapp.model.Appointment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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
        SELECT a.service.id, a.service.nameAr, a.service.nameEn, a.service.nameHe, COUNT(a)
        FROM Appointment a
        WHERE a.startTime BETWEEN :start AND :end
        GROUP BY a.service.id, a.service.nameAr, a.service.nameEn, a.service.nameHe
        ORDER BY COUNT(a) DESC
    """)
    List<Object[]> findServiceStats(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT a FROM Appointment a
        WHERE a.startTime < :endTime
        AND a.endTime > :startTime
    """)
    List<Appointment> findConflictsForUpdate(LocalDateTime startTime, LocalDateTime endTime);
}