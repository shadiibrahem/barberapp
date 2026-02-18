package com.barber.barberapp.repo;

import com.barber.barberapp.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceRepository extends JpaRepository<Service, Long> {
}
