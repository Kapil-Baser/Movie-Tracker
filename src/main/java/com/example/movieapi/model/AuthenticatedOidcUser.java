package com.example.movieapi.model;

import com.example.movieapi.entity.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;

public class AuthenticatedOidcUser extends DefaultOidcUser {

    private final AppUser user;

    public AuthenticatedOidcUser(AppUser user,
                                 Collection<? extends GrantedAuthority> authorities,
                                 OidcIdToken idToken,
                                 OidcUserInfo userInfo) {
        super(authorities, idToken, userInfo, "sub");
        this.user = user;
    }

    public AppUser getUser() {
        return user;
    }

    @Override
    public String getEmail() {
        return user.getEmail();
    }
}
