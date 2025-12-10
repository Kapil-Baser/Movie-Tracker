package com.example.movieapi.controller;

import com.example.movieapi.dto.RegisterUserDto;
import com.example.movieapi.service.RegisterUserValidator;
import com.example.movieapi.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/register")
@Slf4j
public class RegistrationController {

    private final RegisterUserValidator userValidator;
    private final UserService userService;
    private static final String REGISTRATION_PAGE = "register";

    public RegistrationController(RegisterUserValidator userValidator, UserService userService) {
        this.userValidator = userValidator;
        this.userService = userService;
    }


    @ModelAttribute("user")
    public RegisterUserDto registerUserDto() {
        return new RegisterUserDto();
    }


    @GetMapping
    public String showRegisterPage() {
        return REGISTRATION_PAGE;
    }

    @PostMapping("/process-registration")
    public String registration(@Valid @ModelAttribute("user") RegisterUserDto userDto,
                               BindingResult result,
                               Model model) {

        // Validation
        userValidator.validate(userDto, result);

        if (result.hasErrors()) {
            return REGISTRATION_PAGE;
        }

        try {

            userService.registerUser(userDto);
            log.info("User successfully registered: {}", userDto.getUsername());

            return "register-success";
        } catch (Exception e) {

            log.warn("Exception: {}", e.getMessage());
            model.addAttribute("error", "Registration failed. Please try again.");

            return REGISTRATION_PAGE;
        }
    }
}
