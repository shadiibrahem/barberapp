package com.barber.barberapp.controller;

import com.barber.barberapp.model.ShopSettings;
import com.barber.barberapp.repo.GalleryImageRepository;
import com.barber.barberapp.repo.ServiceRepository;
import com.barber.barberapp.repo.ShopSettingsRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ServiceRepository serviceRepo;
    private final GalleryImageRepository galleryImageRepo;
    private final ShopSettingsRepository shopSettingsRepo;

    public HomeController(ServiceRepository serviceRepo,
                          GalleryImageRepository galleryImageRepo,
                          ShopSettingsRepository shopSettingsRepo) {
        this.serviceRepo = serviceRepo;
        this.galleryImageRepo = galleryImageRepo;
        this.shopSettingsRepo = shopSettingsRepo;
    }

    @GetMapping("/")
    public String home(Model model) {

        model.addAttribute("services", serviceRepo.findAll());

        // ✅ IMPORTANT: make sure this line ends with ));
        model.addAttribute("gallery", galleryImageRepo.findByActiveTrueOrderByIdDesc());

        ShopSettings settings = shopSettingsRepo.findById(1L)
                .orElseGet(() -> {
                    ShopSettings s = new ShopSettings();
                    s.setMapsLink("https://www.google.com/maps?q=Majdal+Shams");
                    s.setInstagramLink("https://instagram.com/");
                    return shopSettingsRepo.save(s);
                });

        model.addAttribute("settings", settings);

        return "index";
    }
}