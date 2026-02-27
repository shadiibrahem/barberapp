package com.barber.barberapp.controller;

import com.barber.barberapp.model.GalleryImage;
import com.barber.barberapp.model.Service;
import com.barber.barberapp.model.WorkingHours;
import com.barber.barberapp.repo.DayOffRepository;
import com.barber.barberapp.repo.GalleryImageRepository;
import com.barber.barberapp.repo.ServiceRepository;
import com.barber.barberapp.repo.WorkingHoursRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/services")
public class AdminServiceController {

    private final ServiceRepository serviceRepo;
    private final DayOffRepository dayOffRepo;
    private final WorkingHoursRepository workingHoursRepo;
    private final GalleryImageRepository galleryImageRepo;

    public AdminServiceController(ServiceRepository serviceRepo,
                                  DayOffRepository dayOffRepo,
                                  WorkingHoursRepository workingHoursRepo,
                                  GalleryImageRepository galleryImageRepo) {
        this.serviceRepo = serviceRepo;
        this.dayOffRepo = dayOffRepo;
        this.workingHoursRepo = workingHoursRepo;
        this.galleryImageRepo = galleryImageRepo;
    }

    // 1) List
    @GetMapping
    public String list(Model model) {
        model.addAttribute("services", serviceRepo.findAll());
        model.addAttribute("daysOff", dayOffRepo.findAll());
        model.addAttribute("selectedDate", LocalDate.now().toString());
        model.addAttribute("workingHours",
                workingHoursRepo.findById(1L).orElse(new WorkingHours()));

        // ? IMPORTANT: gallery images for your services-list.html
        model.addAttribute("images", galleryImageRepo.findByActiveTrueOrderByIdDesc());

        return "admin/services-list"; // ? templates/admin/services-list.html
    }

    // 2) Create form
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("service", new Service());
        model.addAttribute("mode", "create");
        return "admin/service-form";
    }

    // 3) Create submit
    @PostMapping
    public String create(@ModelAttribute Service service) {

        service.setNameEn(service.getNameEn() == null ? null : service.getNameEn().trim());
        service.setNameAr(service.getNameAr() == null ? null : service.getNameAr().trim());
        service.setNameHe(service.getNameHe() == null ? null : service.getNameHe().trim());

        if (service.getNameAr() == null || service.getNameAr().isBlank()) {
            return "redirect:/admin/services/new?error=nameAr";
        }

        serviceRepo.save(service);
        return "redirect:/admin/services";
    }

    // 4) Edit form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Service service = serviceRepo.findById(id).orElseThrow();
        model.addAttribute("service", service);
        model.addAttribute("mode", "edit");
        return "admin/service-form";
    }

    // 5) Edit submit
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Long id, @ModelAttribute Service form) {

        Service service = serviceRepo.findById(id).orElseThrow();

        service.setNameEn(form.getNameEn() == null ? null : form.getNameEn().trim());
        service.setNameAr(form.getNameAr() == null ? null : form.getNameAr().trim());
        service.setNameHe(form.getNameHe() == null ? null : form.getNameHe().trim());

        service.setPrice(form.getPrice());
        service.setDurationMinutes(form.getDurationMinutes());
        service.setActive(form.isActive());

        serviceRepo.save(service);
        return "redirect:/admin/services";
    }

    // 6) Delete
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        serviceRepo.deleteById(id);
        return "redirect:/admin/services";
    }

    // 7) Toggle active quickly
    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        Service s = serviceRepo.findById(id).orElseThrow();
        s.setActive(!s.isActive());
        serviceRepo.save(s);
        return "redirect:/admin/services";
    }

    // =========================
    // ? GALLERY inside Services page
    // =========================

    @PostMapping("/gallery/add")
    public String addGallery(@RequestParam String imageUrl) {
        GalleryImage img = new GalleryImage();
        img.setImageUrl(imageUrl.trim());
        img.setActive(true);
        galleryImageRepo.save(img);
        return "redirect:/admin/services";
    }

    @PostMapping("/gallery/delete")
    public String deleteGallery(@RequestParam Long id) {
        galleryImageRepo.deleteById(id);
        return "redirect:/admin/services";
    }
}