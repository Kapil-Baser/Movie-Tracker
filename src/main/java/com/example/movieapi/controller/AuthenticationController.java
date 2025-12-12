package com.example.movieapi.controller;

import com.example.movieapi.dto.*;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.event.PasswordResetEvent;
import com.example.movieapi.exception.ExpiredTokenException;
import com.example.movieapi.exception.InvalidTokenException;
import com.example.movieapi.exception.UserAlreadyVerifiedException;
import com.example.movieapi.service.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {

    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final ResetPasswordValidator resetPasswordValidator;
    private final ApplicationEventPublisher eventPublisher;
    private static final String REGISTRATION_PAGE = "register";
    private static final String FORGET_PASSWORD_PAGE = "forget-password";

    @Autowired
    public AuthenticationController(UserService userService, TokenService tokenService, PasswordResetTokenService passwordResetTokenService, ResetPasswordValidator resetPasswordValidator, ApplicationEventPublisher eventPublisher) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.resetPasswordValidator = resetPasswordValidator;
        this.eventPublisher = eventPublisher;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {

        if (error != null) {
            if (error.equals("disabled")) {
                model.addAttribute("loginError", "Your account has not been verified yet. Please check your email.");
            } else {
                model.addAttribute("loginError", "Invalid username or password!");
            }
        }

        return "login";
    }

    @GetMapping("/forgetPassword")
    public String showForgetPasswordForm(@ModelAttribute("forgetPasswordDto") ForgetPasswordDto dto) {

        return FORGET_PASSWORD_PAGE;
    }

    @PostMapping("/forgetPassSendMail")
    public String sendResetPasswordEmail(@Valid @ModelAttribute("forgetPasswordDto") ForgetPasswordDto dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {

        // Returning early in case email is incorrect
        // If I don't then this error gets added with "No account exists"
        if (result.hasErrors()) {
            return FORGET_PASSWORD_PAGE;
        }

        if (userService.existsByEmail(dto.email())) {
            // Publish Password Reset Event
            eventPublisher.publishEvent(new PasswordResetEvent(dto.email()));
        }

        RedirectInfo redirectInfo = new RedirectInfo("Success", "If an account exists with this email then we have sent a password reset link");

        redirectAttributes.addFlashAttribute("redirectInfo", redirectInfo);

        return "redirect:/auth/login";
    }

    @GetMapping("/resetPassword")
    public String passwordResetPage(@RequestParam("token") String token,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        try {
            passwordResetTokenService.validateToken(token);

        } catch (ExpiredTokenException | InvalidTokenException _) {
            RedirectInfo redirectInfo = new RedirectInfo("Error", "Invalid or expired token");
            redirectAttributes.addFlashAttribute("redirectInfo", redirectInfo);

            return "redirect:/auth/login";
        }

        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken(token);
        model.addAttribute("passwordResetDto", dto);
        return "reset-password";
    }

    @PostMapping("/processResetPassword")
    public String resetPassword(@Valid @ModelAttribute("passwordResetDto") ResetPasswordDto dto,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {

        resetPasswordValidator.validate(dto, result);
        if (result.hasErrors()) {
            return "reset-password";
        }
        // get the user who is requesting the password reset
        AppUser user = passwordResetTokenService.getUser(dto.getToken());
        userService.resetPassword(user, dto.getNewPassword());

        RedirectInfo redirectInfo = new RedirectInfo("Success", "Your password has been changed successfully, you can login now using your new password.");
        redirectAttributes.addFlashAttribute("redirectInfo", redirectInfo);
        return "redirect:/auth/login";
    }
}
