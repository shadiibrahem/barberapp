package com.barber.barberapp.controller;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
public class TestI18nController {

    private final MessageSource messageSource;

    public TestI18nController(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @GetMapping("/test-i18n")
    public String test(@RequestParam(defaultValue = "en") String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        return "app.title = " + messageSource.getMessage("app.title", null, "NOT_FOUND", locale);
    }
}
