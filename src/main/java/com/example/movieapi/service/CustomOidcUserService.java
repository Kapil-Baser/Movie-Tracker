package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Provider;
import com.example.movieapi.model.AuthenticatedOidcUser;
import com.example.movieapi.repository.RoleRepository;
import com.example.movieapi.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
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

        String googleSubject = oidcUser.getSubject();
        String email = oidcUser.getEmail();

        if (!Boolean.TRUE.equals(oidcUser.getEmailVerified())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unverified_email"),
                    "Google account email is not verified"
            );
        }

        // Loading the User, if user does not exist then create a new one
        AppUser appUser = userRepository.findByProviderAndProviderId(Provider.GOOGLE, oidcUser.getSubject())
                        .orElseGet(() ->
                            createUser(googleSubject, email));


        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        authorities.add(
                new SimpleGrantedAuthority(
                        appUser.getRole().getName()
                )
        );

        // Taking in the already authenticated identity by Google and translating it to an identity that my app understands
        return new AuthenticatedOidcUser(appUser, authorities, userRequest.getIdToken(), oidcUser.getUserInfo());
    }

    private AppUser createUser(String googleSubject, String email) {
        AppUser newUser = new AppUser();

        newUser.setEmail(email);
        newUser.setEnabled(true);
        newUser.setUsername(googleSubject);
        newUser.setProvider(Provider.GOOGLE);
        newUser.setProviderId(googleSubject);
        newUser.setPassword(null);
        newUser.setRole(roleRepository.findByName("ROLE_USER"));

        return userRepository.save(newUser);
    }
}
