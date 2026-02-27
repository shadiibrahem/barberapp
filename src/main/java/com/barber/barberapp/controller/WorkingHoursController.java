package com.barber.barberapp.controller;

import com.barber.barberapp.model.WorkingHours;
import com.barber.barberapp.repo.WorkingHoursRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WorkingHoursController {

    private final WorkingHoursRepository repo;

    public WorkingHoursController(WorkingHoursRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/admin/hours/save")
    public String save(@RequestParam String openTime,
                       @RequestParam String closeTime,
                       @RequestParam Integer slotStepMinutes) {

        WorkingHours wh = repo.findById(1L).orElse(new WorkingHours());
        wh.setId(1L);
        wh.setOpenTime(java.time.LocalTime.parse(openTime));
        wh.setCloseTime(java.time.LocalTime.parse(closeTime));
        wh.setSlotStepMinutes(slotStepMinutes);

        repo.save(wh);
        return "redirect:/admin/services";
    }
}
