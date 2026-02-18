package com.barber.barberapp.repo;

import com.barber.barberapp.model.DayOff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DayOffRepository extends JpaRepository<DayOff, Long> {
    boolean existsByOffDate(LocalDate offDate);
    List<DayOff> findAllByOrderByOffDateAsc();
    void deleteByOffDate(LocalDate offDate);
}
