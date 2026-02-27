package com.barber.barberapp.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import com.barber.barberapp.service.WhatsAppService;
import com.barber.barberapp.repo.DayOffRepository;
import com.barber.barberapp.model.WorkingHours;
import com.barber.barberapp.repo.WorkingHoursRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.MessageSource;
import java.util.Locale;
import org.springframework.context.i18n.LocaleContextHolder;


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
    private final WorkingHoursRepository workingHoursRepo;
    private final MessageSource messageSource;


    public BookingController(ServiceRepository serviceRepo,
                             AppointmentRepository appointmentRepo,
                             WhatsAppService whatsAppService,
                             DayOffRepository dayOffRepo,
                             WorkingHoursRepository workingHoursRepo,
                             MessageSource messageSource) {
        this.serviceRepo = serviceRepo;
        this.appointmentRepo = appointmentRepo;
        this.whatsAppService = whatsAppService;
        this.dayOffRepo = dayOffRepo;
        this.workingHoursRepo = workingHoursRepo;
        this.messageSource = messageSource;
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
            model.addAttribute("dayOffMessage", messageSource.getMessage("dayoff.message", null, LocaleContextHolder.getLocale()));
            return "book";
        }

        List<Service> services = serviceRepo.findByActiveTrue();
        model.addAttribute("services", services);

        Long selectedServiceId = (serviceId == null && !services.isEmpty())
                ? services.get(0).getId()
                : serviceId;

        model.addAttribute("selectedServiceId", selectedServiceId);
        model.addAttribute("selectedDate", selectedDate.toString());

        WorkingHours wh = workingHoursRepo.findById(1L).orElse(new WorkingHours());

        LocalTime open = wh.getOpenTime();
        LocalTime close = wh.getCloseTime();
        int stepMinutes = wh.getSlotStepMinutes() == null ? 5 : wh.getSlotStepMinutes();

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
    @Transactional
    public String createBooking(@RequestParam String customerName,
                                @RequestParam String phone,
                                @RequestParam Long serviceId,
                                @RequestParam String date,
                                @RequestParam String time,
                                Model model,Locale locale) {

        Service service = serviceRepo.findById(serviceId).orElseThrow();

        LocalDate d = LocalDate.parse(date);
        LocalTime t = LocalTime.parse(time);
        LocalDateTime startTime = LocalDateTime.of(d, t);
        LocalDateTime endTime = startTime.plusMinutes(service.getDurationMinutes());

        List<Appointment> conflicts =
                appointmentRepo.findConflictsForUpdate(startTime, endTime);

        if (!conflicts.isEmpty()) {
            return "redirect:/book?date=" + date +
                    "&serviceId=" + serviceId + "&error=1";
        }



        Appointment a = new Appointment();

        a.setCustomerName(customerName);
        a.setPhone(phone);
        a.setService(service);
        a.setStartTime(startTime);
        a.setEndTime(endTime);
        a.setStatus("BOOKED");

        appointmentRepo.save(a);

        String serviceName;

        if (locale.getLanguage().equals("ar")) {
            serviceName = service.getNameAr();
        } else if (locale.getLanguage().equals("he")) {
            serviceName = service.getNameHe();
        } else {
            serviceName = service.getNameEn();
        }

        String msg = "\n✅ " + messageSource.getMessage("wa.confirm", null, locale)
                + "\n" + messageSource.getMessage("wa.name", null, locale) + ": " + customerName
                + "\n" + messageSource.getMessage("wa.service", null, locale) + ": " + serviceName
                + "\n" + messageSource.getMessage("wa.date", null, locale) + ": " + date
                + "\n" + messageSource.getMessage("wa.time", null, locale) + ": " + time;

        String whatsappUrl = "https://wa.me/" + barberWhatsapp
                + "?text=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);

        model.addAttribute("whatsappUrl", whatsappUrl);
        return "success";
    }

}
