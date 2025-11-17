package com.example.movieapi.service;

import com.example.movieapi.dto.RegisterUserDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.RoleRepository;
import com.example.movieapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<AppUser> loadUserByEmail(String email) {
        return repository.findByEmail(email);
    }

    public AppUser register(RegisterUserDto dto) {
        AppUser newUser = new AppUser(dto.getUsername(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                true,
                roleRepository.findByName("ROLE_USER")
                );

        return repository.save(newUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = repository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        var authenticatedUser = new AuthenticatedUser(user);
        log.info("Username {} authorities {}", authenticatedUser.getUser(), authenticatedUser.getAuthorities());
        return authenticatedUser;
    }
}
