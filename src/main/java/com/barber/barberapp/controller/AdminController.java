package com.barber.barberapp.controller;

import com.barber.barberapp.model.*;
import com.barber.barberapp.repo.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Controller
public class AdminController {

    private final AppointmentRepository appointmentRepo;
    private final DayOffRepository dayOffRepo;
    private final GalleryImageRepository galleryImageRepo;
    private final ShopSettingsRepository shopSettingsRepo;

    public AdminController(AppointmentRepository appointmentRepo,
                           DayOffRepository dayOffRepo,
                           GalleryImageRepository galleryImageRepo,
                           ShopSettingsRepository shopSettingsRepo) {

        this.appointmentRepo = appointmentRepo;
        this.dayOffRepo = dayOffRepo;
        this.galleryImageRepo = galleryImageRepo;
        this.shopSettingsRepo = shopSettingsRepo;
    }

    // ==============================
    // ADMIN DASHBOARD
    // ==============================

    @GetMapping("/admin")
    public String adminPage(@RequestParam(required = false) String date,
                            Model model,
                            Locale locale) {

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

        LocalDate weekStartDate =
                selectedDate.minusDays(selectedDate.getDayOfWeek().getValue() - 1);

        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEnd = weekStartDate.plusDays(7).atStartOfDay();

        long weekCount = appointmentRepo.countByStartTimeBetween(weekStart, weekEnd);

        model.addAttribute("dailyCount", todayCount);
        model.addAttribute("weeklyCount", weekCount);

        // ===== TOP SERVICE =====
        List<Object[]> stats = appointmentRepo.findServiceStats(weekStart, weekEnd);

        String lang = locale.getLanguage();
        String topService = lang.equals("ar") ? "لا يوجد"
                : (lang.equals("he") ? "אין" : "None");

        if (!stats.isEmpty()) {
            Object[] row = stats.get(0);

            // Based on your existing query ordering:
            // row[1]=nameAr, row[2]=nameEn, row[3]=nameHe
            String nameAr = (String) row[1];
            String nameEn = (String) row[2];
            String nameHe = (String) row[3];

            if ("he".equals(lang)) {
                topService = (nameHe != null && !nameHe.isBlank()) ? nameHe : topService;
            } else if ("en".equals(lang)) {
                topService = (nameEn != null && !nameEn.isBlank()) ? nameEn : topService;
            } else {
                topService = (nameAr != null && !nameAr.isBlank()) ? nameAr : topService;
            }
        }

        model.addAttribute("topService", topService);

        model.addAttribute("appointments", appointments);
        model.addAttribute("selectedDate", selectedDate.toString());
        model.addAttribute("daysOff", dayOffRepo.findAllByOrderByOffDateAsc());

        return "admin";
    }

    // ==============================
    // APPOINTMENT DELETE
    // ==============================

    @PostMapping("/admin/delete")
    public String delete(@RequestParam Long id,
                         @RequestParam(required = false) String date) {

        appointmentRepo.deleteById(id);

        if (date == null || date.isBlank())
            return "redirect:/admin";

        return "redirect:/admin?date=" + date;
    }

    // ==============================
    // DAY OFF
    // ==============================

    @PostMapping("/admin/dayoff/add")
    public String addDayOff(@RequestParam String offDate,
                            @RequestParam(required = false) String reason) {

        LocalDate d = LocalDate.parse(offDate);

        if (!dayOffRepo.existsByOffDate(d)) {
            dayOffRepo.save(new DayOff(null, d, (reason == null ? "" : reason)));
        }

        return "redirect:/admin/services";
    }

    @PostMapping("/admin/dayoff/delete")
    public String deleteDayOff(@RequestParam String offDate) {

        dayOffRepo.deleteByOffDate(LocalDate.parse(offDate));

        return "redirect:/admin/services";
    }

    // ==============================
    // GALLERY (URL ADD)
    // ==============================

    @PostMapping("/admin/gallery/add")
    public String addGalleryImage(@RequestParam String imageUrl) {

        if (imageUrl == null || imageUrl.isBlank()) {
            return "redirect:/admin/services?error=emptyUrl";
        }

        GalleryImage img = new GalleryImage();
        img.setImageUrl(imageUrl.trim());
        img.setActive(true);

        galleryImageRepo.save(img);

        return "redirect:/admin/services";
    }

    // ==============================
    // GALLERY (UPLOAD FROM PHONE)
    // ==============================

    @PostMapping("/admin/gallery/upload")
    public String uploadGalleryImage(@RequestParam("file") MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            return "redirect:/admin/services?error=emptyFile";
        }

        // Validate content type (basic)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return "redirect:/admin/services?error=notImage";
        }

        // Ensure folder exists
        Path uploadDir = Paths.get("uploads");
        Files.createDirectories(uploadDir);

        // Get safe extension
        String originalName = file.getOriginalFilename();
        String extension = ".jpg"; // default

        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            // allow only common safe extensions
            if (ext.equals(".jpg") || ext.equals(".jpeg") || ext.equals(".png") || ext.equals(".webp")) {
                extension = ext.equals(".jpeg") ? ".jpg" : ext;
            }
        }

        String fileName = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(fileName);

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        GalleryImage image = new GalleryImage();
        image.setImageUrl("/uploads/" + fileName);
        image.setActive(true);
        galleryImageRepo.save(image);

        return "redirect:/admin/services?success=upload";
    }

    @PostMapping("/admin/gallery/delete")
    public String deleteGalleryImage(@RequestParam Long id) {

        galleryImageRepo.deleteById(id);

        return "redirect:/admin/services";
    }

    // ==============================
    // SETTINGS
    // ==============================

    @GetMapping("/admin/settings")
    public String settingsAdmin(Model model) {

        ShopSettings s = shopSettingsRepo.findById(1L)
                .orElseGet(() -> shopSettingsRepo.save(new ShopSettings()));

        model.addAttribute("settings", s);

        return "admin-settings";
    }

    @PostMapping("/admin/settings/save")
    public String saveSettings(@RequestParam String mapsLink,
                               @RequestParam String instagramLink) {

        ShopSettings s = shopSettingsRepo.findById(1L)
                .orElseGet(ShopSettings::new);

        s.setMapsLink(mapsLink);
        s.setInstagramLink(instagramLink);

        shopSettingsRepo.save(s);

        return "redirect:/admin/settings";
    }
}