package com.barber.barberapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${meta.whatsapp.phoneNumberId}")
    private String phoneNumberId;

    @Value("${meta.whatsapp.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendTextMessage(String toDigitsOnly, String text) {
        String url = "https://graph.facebook.com/v20.0/" + phoneNumberId + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", toDigitsOnly);  // example: 972502189213
        body.put("type", "text");

        Map<String, String> textObj = new HashMap<>();
        textObj.put("body", text);
        body.put("text", textObj);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }
}
