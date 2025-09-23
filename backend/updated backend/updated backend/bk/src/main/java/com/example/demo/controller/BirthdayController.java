package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.BirthdayEmailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class BirthdayController {

    private final BirthdayEmailService birthdayEmailService;

    @PostMapping("/send-birthday-emails")
    public String triggerBirthdayEmails() {
        birthdayEmailService.sendBirthdayEmails();
        return "Birthday emails sent!";
    }
}
