package com.barber.barberapp.controller;

import com.barber.barberapp.repo.ServiceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ServiceRepository serviceRepo;

    public HomeController(ServiceRepository serviceRepo) {
        this.serviceRepo = serviceRepo;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("services", serviceRepo.findAll());
        return "index";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

}
