package com.example.movieapi.controller;

import com.example.movieapi.dto.RegisterUserDto;
import com.example.movieapi.service.RegisterUserValidator;
import com.example.movieapi.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@Slf4j
public class AuthenticationController {

    private final UserService userService;
    private final RegisterUserValidator userValidator;

    @Autowired
    public AuthenticationController(UserService userService, RegisterUserValidator userValidator) {
        this.userService = userService;
        this.userValidator = userValidator;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password!");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new RegisterUserDto());
        return "register";
    }

    @PostMapping("/register/save")
    public String registration(@Valid @ModelAttribute("user") RegisterUserDto userDto,
                               BindingResult result,
                               Model model) {

        // Validation
        userValidator.validate(userDto, result);

        if (result.hasErrors()) {
            return "register";
        }

        try {

            userService.register(userDto);
            log.info("User successfully registered: {}", userDto.getUsername());

            return "register-success";
        } catch (Exception e) {

            log.warn("Exception: {}", e.getMessage());
            model.addAttribute("error", "Registration failed. Please try again.");

            return "register";
        }


    }

/*    @PostMapping("/register")
    public ResponseEntity<AppUser> register(@RequestBody RegisterUserDto registerUserDto) {
        return ResponseEntity.ok(userService.register(registerUserDto));
    }*/
}
