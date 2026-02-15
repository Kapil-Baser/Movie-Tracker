package com.example.movieapi.service;

import com.example.movieapi.dto.MovieDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.sql.Date;
import java.time.LocalDate;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public MailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendPlainText(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setSentDate(Date.valueOf(LocalDate.now()));
        mailSender.send(message);
    }

    public void sendHtml(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true means this is HTML
        mailSender.send(message);
    }

    public void sendRegistrationEmail(String to, String name, String link) throws MessagingException {
        // Thymeleaf context
        Context context = new Context();
        context.setVariable("userName", name);
        context.setVariable("verificationUrl", link);

        // Process HTML Template
        String htmlBody = templateEngine.process("/mail/registration-mail", context);

        // Create and send Mime Message
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Registration Confirmation");
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String link) throws MessagingException {
        Context context = new Context();
        context.setVariable("resetUrl", link);

        String htmlBody = templateEngine.process("/mail/reset-password", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Reset Your Password");
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }

    public void sendMovieOutForStreamingEmail(String to, MovieDto movie) throws MessagingException {
        Context context = new Context();
        context.setVariable("movie", movie);

        String htmlBody = templateEngine.process("/mail/out-for-streaming", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(movie.title() + " is streaming now");
        helper.setText(htmlBody, true);

        mailSender.send(message);
    }
}
