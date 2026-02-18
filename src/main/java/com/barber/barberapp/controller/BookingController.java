package com.barber.barberapp.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import com.barber.barberapp.service.WhatsAppService;
import com.barber.barberapp.repo.DayOffRepository;




import com.barber.barberapp.model.Appointment;
import com.barber.barberapp.model.Service;
import com.barber.barberapp.repo.AppointmentRepository;
import com.barber.barberapp.repo.ServiceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class BookingController {

    private final ServiceRepository serviceRepo;
    private final AppointmentRepository appointmentRepo;
    @Value("${barber.whatsapp}")
    private String barberWhatsapp;
    private final WhatsAppService whatsAppService;
    private final DayOffRepository dayOffRepo;


    public BookingController(ServiceRepository serviceRepo,
                             AppointmentRepository appointmentRepo,
                             WhatsAppService whatsAppService,
                             DayOffRepository dayOffRepo) {
        this.serviceRepo = serviceRepo;
        this.appointmentRepo = appointmentRepo;
        this.whatsAppService = whatsAppService;
        this.dayOffRepo = dayOffRepo;
    }



    @GetMapping("/book")
    public String bookPage(@RequestParam(required = false) String date,
                           @RequestParam(required = false) Long serviceId,
                           Model model) {

        LocalDate selectedDate = (date == null || date.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(date);

        boolean isDayOff = dayOffRepo.existsByOffDate(selectedDate);
        model.addAttribute("isDayOff", isDayOff);

        if (isDayOff) {
            model.addAttribute("slots", List.of()); // no slots
            model.addAttribute("dayOffMessage", "هذا اليوم عطلة / مغلق. اختر يومًا آخر.");
            return "book";
        }

        List<Service> services = serviceRepo.findAll();
        model.addAttribute("services", services);

        Long selectedServiceId = (serviceId == null && !services.isEmpty())
                ? services.get(0).getId()
                : serviceId;

        model.addAttribute("selectedServiceId", selectedServiceId);
        model.addAttribute("selectedDate", selectedDate.toString());

        // working hours
        LocalTime open = LocalTime.of(10, 0);
        LocalTime close = LocalTime.of(19, 0);

        // step for generating times (5 minutes supports 15/20/30 nicely)
        int stepMinutes = 5;

        // selected service duration
        int durationMinutes = 30; // fallback
        if (selectedServiceId != null) {
            Service selectedService = serviceRepo.findById(selectedServiceId).orElse(null);
            if (selectedService != null) durationMinutes = selectedService.getDurationMinutes();
        }

        // booked appointments for this day
        LocalDateTime dayStart = selectedDate.atStartOfDay();
        LocalDateTime dayEnd = selectedDate.plusDays(1).atStartOfDay();
        List<Appointment> booked = appointmentRepo.findByStartTimeBetween(dayStart, dayEnd);

        List<String> availableSlots = new ArrayList<>();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        for (LocalTime lt = open; !lt.plusMinutes(durationMinutes).isAfter(close); lt = lt.plusMinutes(stepMinutes)) {
            LocalDateTime start = selectedDate.atTime(lt);
            LocalDateTime end = start.plusMinutes(durationMinutes);

            boolean conflict = false;
            for (Appointment a : booked) {
                // overlap: start < a.end && end > a.start
                if (start.isBefore(a.getEndTime()) && end.isAfter(a.getStartTime())) {
                    conflict = true;
                    break;
                }
            }

            if (!conflict) {
                availableSlots.add(lt.format(fmt));
            }
        }

        model.addAttribute("slots", availableSlots);
        return "book";
    }


    @PostMapping("/book")
    public String createBooking(@RequestParam String customerName,
                                @RequestParam String phone,
                                @RequestParam Long serviceId,
                                @RequestParam String date,
                                @RequestParam String time,
                                Model model) {

        Service service = serviceRepo.findById(serviceId).orElseThrow();

        LocalDate d = LocalDate.parse(date);
        LocalTime t = LocalTime.parse(time);
        LocalDateTime startTime = LocalDateTime.of(d, t);
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        boolean alreadyBooked = appointmentRepo
                .existsByStartTimeLessThanAndEndTimeGreaterThan(endTime, startTime);

        if (alreadyBooked) {
            return "redirect:/book?date=" + date + "&serviceId=" + serviceId + "&error=1";
        }


        Appointment a = new Appointment(null, customerName, phone, service, startTime, endTime, "BOOKED");
        appointmentRepo.save(a);

        String msg = "✅ تم تأكيد الحجز\n"
                + "الاسم: " + customerName + "\n"
                + "الخدمة: " + service.getName() + "\n"
                + "التاريخ: " + date + "\n"
                + "الوقت: " + time;

        String whatsappUrl = "https://wa.me/" + barberWhatsapp
                + "?text=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);

        model.addAttribute("whatsappUrl", whatsappUrl);



        return "success";





    }
}
