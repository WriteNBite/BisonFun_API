package com.writenbite.bisonfun.api.security;

import com.writenbite.bisonfun.api.database.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class UserPrincipal implements UserDetails {
    String userName;
    String password;
    Set<GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this(user, new HashSet<>());
    }

    public UserPrincipal(User user, Set<GrantedAuthority> authorities) {
        userName = user.getUsername();
        password = user.getPassword();
        this.authorities = authorities;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }
}
