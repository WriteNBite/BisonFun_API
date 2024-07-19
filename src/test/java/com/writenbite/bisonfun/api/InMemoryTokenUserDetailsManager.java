package com.writenbite.bisonfun.api;

import com.writenbite.bisonfun.api.security.TokenType;
import com.writenbite.bisonfun.api.service.TokenUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

public class InMemoryTokenUserDetailsManager implements TokenUserDetailsService {
    private final InMemoryUserDetailsManager inMemoryUserDetailsManager;

    public InMemoryTokenUserDetailsManager(InMemoryUserDetailsManager inMemoryUserDetailsManager) {
        this.inMemoryUserDetailsManager = inMemoryUserDetailsManager;
    }

    @Override
    public UserDetails loadUserByUsernameAndGrantTokenAuthority(String username, TokenType tokenType) {
        return inMemoryUserDetailsManager.loadUserByUsername(username);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return inMemoryUserDetailsManager.loadUserByUsername(username);
    }
}
