package com.barber.barberapp.config;

import com.barber.barberapp.model.Service;
import com.barber.barberapp.repo.ServiceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.barber.barberapp.model.WorkingHours;
import com.barber.barberapp.repo.WorkingHoursRepository;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedServices(ServiceRepository serviceRepo,
                                   WorkingHoursRepository workingHoursRepo) {
        return args -> {
            if (serviceRepo.count() == 0) {

                Service s1 = new Service();
                s1.setNameEn("Haircut + Beard");
                s1.setNameAr("قص شعر + ذقن");
                s1.setNameHe("תספורת + זקן");
                s1.setPrice(70);
                s1.setDurationMinutes(40);
                s1.setActive(true);
                serviceRepo.save(s1);

                Service s2 = new Service();
                s2.setNameEn("Haircut");
                s2.setNameAr("قص شعر");
                s2.setNameHe("תספורת");
                s2.setPrice(50);
                s2.setDurationMinutes(25);
                s2.setActive(true);
                serviceRepo.save(s2);

                Service s3 = new Service();
                s3.setNameEn("Beard");
                s3.setNameAr("دقن");
                s3.setNameHe("זקן");
                s3.setPrice(30);
                s3.setDurationMinutes(15);
                s3.setActive(true);
                serviceRepo.save(s3);


            }
            if (!workingHoursRepo.existsById(1L)) {
                workingHoursRepo.save(new WorkingHours(1L,
                        java.time.LocalTime.of(10,0),
                        java.time.LocalTime.of(19,0),
                        5));
            }
        };
    }
}
