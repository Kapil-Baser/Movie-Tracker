package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Provider;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.RoleRepository;
import com.example.movieapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOidcUserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        OAuth2AccessToken accessToken = userRequest.getAccessToken();

        // Loading the User if user does not exist then create a new one
        AppUser appUser = userRepository.findByEmail(oidcUser.getEmail())
                        .orElseGet(() -> {
                            AppUser newUser = new AppUser();
                            newUser.setEmail(oidcUser.getEmail());
                            newUser.setUsername(oidcUser.getName());
                            newUser.setPassword(null);
                            newUser.setProvider(Provider.GOOGLE);
                            newUser.setProviderId(oidcUser.getSubject());
                            newUser.setGoogleAccessToken(accessToken.getTokenValue());
                            newUser.setGoogleTokenExpires(accessToken.getExpiresAt());
                            newUser.setEnabled(true);
                            newUser.setRole(roleRepository.findByName("ROLE_USER"));
                            return userRepository.save(newUser);
                        });


        log.info("OidcUser loaded: {}", oidcUser);
        return new AuthenticatedUser(appUser, oidcUser.getAttributes(), oidcUser.getIdToken(), oidcUser.getUserInfo());
    }
}
