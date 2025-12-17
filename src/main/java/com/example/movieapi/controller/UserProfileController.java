package com.example.movieapi.controller;

import com.example.movieapi.dto.ChangePasswordDto;
import com.example.movieapi.dto.RedirectInfo;
import com.example.movieapi.dto.UserPasswordDto;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.ChangePasswordValidator;
import com.example.movieapi.service.MovieCollectionService;
import com.example.movieapi.service.UserService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
public class UserProfileController {

    private final MovieCollectionService collectionService;
    private final ChangePasswordValidator changePasswordValidator;
    private final UserService userService;

    @Autowired
    public UserProfileController(MovieCollectionService collectionService, ChangePasswordValidator changePasswordValidator, UserService userService) {
        this.collectionService = collectionService;
        this.changePasswordValidator = changePasswordValidator;
        this.userService = userService;
    }

    @ModelAttribute
    public void addCollectionSize(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {
        if (authenticatedUser != null) {
            int collectionCount = collectionService.getCollectionCount(authenticatedUser);
            model.addAttribute("collectionCount", collectionCount);
        }
    }

    @GetMapping("/profile")
    public String showProfilePage() {
        return "profile-settings";
    }

    @HxRequest
    @GetMapping("/dashboard")
    public String userDashBoard(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {
        int collectionCount = collectionService.getCollectionCount(authenticatedUser);
        model.addAttribute("collectionCount", collectionCount);

        return "fragments/profile :: dashboard";
    }

    @HxRequest
    @GetMapping("/account/settings")
    public String showAccountSettingsPage(@ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto) {

        return "fragments/change-password :: password-change";
    }

    @HxRequest
    @PostMapping("/account/settings/process-change-password")
    public String changePassword(@Valid @ModelAttribute("changePasswordDto") ChangePasswordDto changePasswordDto,
                                 BindingResult result,
                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest request) {

        // Validating the submitted changePasswordDto
        changePasswordValidator.validate(changePasswordDto, result);

        if (result.hasErrors()) {
            return "fragments/change-password :: password-change";
        }

        // Changing the password and saving the user
        //userService.changePassword(authenticatedUser, changePasswordDto.getNewPassword());

        // Invalidate the current session and log the current user out
        userService.performLogout(request);

        redirectAttributes.addFlashAttribute("passwordChangeSuccess", true);

        return "redirect:htmx:/auth/login";
    }

    @GetMapping("/deactivate-modal")
    public String showDeactivateModal(@ModelAttribute("userPasswordDto") UserPasswordDto dto) {
        return "fragments/modal :: deactivate-form";
    }

    @PostMapping("/deactivate-account")
    public String deactivateUser(@ModelAttribute("userPasswordDto") UserPasswordDto dto,
                                 BindingResult result,
                                 @AuthenticationPrincipal AuthenticatedUser user,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {

        if(result.hasErrors() || !userService.isValidPassword(user.getUser(), dto.currentPassword())) {
            result.rejectValue("currentPassword", "password.incorrect", "Current password is incorrect");
            return "fragments/modal :: deactivate-form";
        }
        // Disable the user account and invalidate the session
        //userService.deactivateUser(user);
        userService.performLogout(request);

        RedirectInfo redirectInfo = new RedirectInfo("Success", "Your account has been disabled as per your request.");
        redirectAttributes.addFlashAttribute("redirectInfo", redirectInfo);

        return "redirect:htmx:/auth/login";
    }
}
