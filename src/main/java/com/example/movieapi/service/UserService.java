package com.example.movieapi.service;

import com.example.movieapi.dto.RegisterUserDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.event.TokenResendEvent;
import com.example.movieapi.event.UserRegisteredEvent;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.RoleRepository;
import com.example.movieapi.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserService(UserRepository repository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    public AppUser loadUserByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Unregistered email"));
    }

    public boolean existsByEmail(String email) {
        return repository.existsByEmail(email);
    }

    public void registerUser(RegisterUserDto dto) {
        AppUser newUser = new AppUser(
                dto.getUsername(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                false,
                roleRepository.findByName("ROLE_USER")
                );

        AppUser savedUser = repository.save(newUser);

        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(("User not found")));
        var authenticatedUser = new AuthenticatedUser(user);
        log.info("Username {} authorities {}", authenticatedUser.getUser(), authenticatedUser.getAuthorities());
        return authenticatedUser;
    }

    public void changePassword(AuthenticatedUser authenticatedUser, String newPassword) {

        AppUser user = authenticatedUser.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

        repository.save(user);

    }

    public void performLogout(HttpServletRequest request) {
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, null, null);
    }

    public void resendToken(String email) {
        AppUser user = loadUserByEmail(email);

        eventPublisher.publishEvent(new TokenResendEvent(user));
    }

    public void resetPassword(AppUser user, String newPassword) {
        // Set the new password
        user.setPassword(passwordEncoder.encode(newPassword));
        repository.save(user);
    }

    public boolean isValidPassword(AppUser user, String password) {
        return passwordEncoder.matches(password, user.getPassword());
    }

    public void deactivateUser(AuthenticatedUser authenticatedUser) {
        AppUser user = authenticatedUser.getUser();
        user.setEnabled(false);
        repository.save(user);
    }
}
