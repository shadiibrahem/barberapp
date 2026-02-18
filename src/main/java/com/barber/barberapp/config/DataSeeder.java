package com.barber.barberapp.config;

import com.barber.barberapp.model.Service;
import com.barber.barberapp.repo.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedServices(ServiceRepository serviceRepo) {
        return args -> {
            if (serviceRepo.count() == 0) {

                serviceRepo.save(new Service(null, "قص شعر + دقن", 70, 30));
                serviceRepo.save(new Service(null, "قص شعر", 50, 20));
                serviceRepo.save(new Service(null, "دقن", 30, 15));


            }
        };
    }
}
