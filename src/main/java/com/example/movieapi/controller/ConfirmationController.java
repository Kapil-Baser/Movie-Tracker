package com.example.movieapi.controller;

import com.example.movieapi.dto.RedirectInfo;
import com.example.movieapi.dto.UserEmailDto;
import com.example.movieapi.exception.ExpiredTokenException;
import com.example.movieapi.exception.InvalidTokenException;
import com.example.movieapi.exception.UserAlreadyVerifiedException;
import com.example.movieapi.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@Slf4j
public class ConfirmationController {

    private final EmailVerificationService emailVerificationService;
    private static final String RESEND_TOKEN_PAGE = "resend-verificationToken-form";
    private static final String REDIRECT_SUCCESS = "Success";
    private static final String REDIRECT_INFO = "redirectInfo";

    public ConfirmationController(EmailVerificationService emailVerificationService) {
        this.emailVerificationService = emailVerificationService;
    }

    @GetMapping("/confirm")
    public String confirmToken(@RequestParam("token") String token,
                               Model model, RedirectAttributes redirectAttributes,
                               @ModelAttribute("userEmailDto") UserEmailDto dto) {

        try {
            emailVerificationService.confirmAndEnable(token);

            RedirectInfo redirectInfo = new RedirectInfo(REDIRECT_SUCCESS, "Account verified successfully. You can now login");
            redirectAttributes.addFlashAttribute(REDIRECT_INFO, redirectInfo);

        } catch (ExpiredTokenException | InvalidTokenException e) {

            model.addAttribute("errorMessage", e.getMessage());
            return RESEND_TOKEN_PAGE;

        } catch (UserAlreadyVerifiedException e) {
            RedirectInfo redirectInfo = new RedirectInfo(REDIRECT_SUCCESS, e.getMessage());
            redirectAttributes.addFlashAttribute(REDIRECT_INFO, redirectInfo);
        }

        return "redirect:/auth/login";
    }

    @PostMapping("/resendToken")
    public String resendVerificationToken(@Valid @ModelAttribute("userEmailDto") UserEmailDto dto,
                                          BindingResult result,
                                          RedirectAttributes redirectAttributes) {
        if(result.hasErrors()) {
            return RESEND_TOKEN_PAGE;
        }

        try {
            emailVerificationService.createToken(dto.email());
        } catch (UsernameNotFoundException e) {
            log.warn("Account not found with email: {}, message: {}", dto.email(), e.getMessage());
        }

        RedirectInfo redirectInfo = new RedirectInfo(REDIRECT_SUCCESS,
                "If an account exists with this email then a verification token has been sent. Please check your email.");
        redirectAttributes.addFlashAttribute(REDIRECT_INFO, redirectInfo);
        return "redirect:/auth/login";
    }
}
