package com.example.movieapi.controller;

import com.example.movieapi.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/email")
@Slf4j
public class EmailController {

    private final MailService mailService;

    public EmailController(MailService mailService) {
        this.mailService = mailService;
    }

    @PostMapping("/send")
    public String sendEmail(@RequestParam("sendTo") String to,
                            @RequestParam String subject,
                            @RequestParam String body) {
        try {
            mailService.sendPlainText(to, subject, body);
            log.info("Email sent successfully to {}", to);
        } catch (MailException e) {
            log.warn("Error sending email to {}", to, e);
        }

        return "Email sent";
    }
}
