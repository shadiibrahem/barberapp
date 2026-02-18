package com.barber.barberapp.controller;

import com.barber.barberapp.model.Appointment;
import com.barber.barberapp.repo.AppointmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.barber.barberapp.model.DayOff;
import com.barber.barberapp.repo.DayOffRepository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AdminController {

    private final AppointmentRepository appointmentRepo;
    private final DayOffRepository dayOffRepo;

    public AdminController(AppointmentRepository appointmentRepo, DayOffRepository dayOffRepo) {
        this.appointmentRepo = appointmentRepo;
        this.dayOffRepo = dayOffRepo;
    }


    @GetMapping("/admin")
    public String adminPage(@RequestParam(required = false) String date, Model model) {

        LocalDate selectedDate;

        try {
            selectedDate = (date == null || date.isBlank())
                    ? LocalDate.now()
                    : LocalDate.parse(date);
        } catch (Exception e) {
            selectedDate = LocalDate.now();
        }

        LocalDateTime start = selectedDate.atStartOfDay();
        LocalDateTime end = selectedDate.plusDays(1).atStartOfDay();

        List<Appointment> appointments =
                appointmentRepo.findByStartTimeBetweenOrderByStartTimeAsc(start, end);

        long todayCount = appointmentRepo.countByStartTimeBetween(start, end);

        LocalDate weekStartDate = selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1); // Monday
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStartDate.plusDays(7).atStartOfDay();

        long weekCount = appointmentRepo.countByStartTimeBetween(weekStart, weekEnd);

        // ✅ match admin.html variables:
        model.addAttribute("dailyCount", todayCount);
        model.addAttribute("weeklyCount", weekCount);

        List<Object[]> stats = appointmentRepo.findServiceStats(weekStart, weekEnd);
        String topService = "لا يوجد";
        if (!stats.isEmpty()) {
            topService = (String) stats.get(0)[0];
        }
        model.addAttribute("topService", topService);

        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedDate", selectedDate.toString());
        model.addAttribute("daysOff", dayOffRepo.findAllByOrderByOffDateAsc());


        return "admin";
    }

    @PostMapping("/admin/delete")
    public String delete(@RequestParam Long id, @RequestParam(required = false) String date) {
        appointmentRepo.deleteById(id);
        if (date == null || date.isBlank()) return "redirect:/admin";
        return "redirect:/admin?date=" + date;
    }

    @PostMapping("/admin/dayoff/add")
    public String addDayOff(@RequestParam String offDate,
                            @RequestParam(required = false) String reason) {

        LocalDate d = LocalDate.parse(offDate);

        if (!dayOffRepo.existsByOffDate(d)) {
            dayOffRepo.save(new DayOff(null, d, (reason == null ? "" : reason)));
        }
        return "redirect:/admin?date=" + offDate;
    }

    @PostMapping("/admin/dayoff/delete")
    public String deleteDayOff(@RequestParam String offDate,
                               @RequestParam(required = false) String date) {
        dayOffRepo.deleteByOffDate(LocalDate.parse(offDate));
        if (date == null || date.isBlank()) return "redirect:/admin";
        return "redirect:/admin?date=" + date;
    }


}
