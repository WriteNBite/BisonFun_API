package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.security.TokenType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface TokenUserDetailsService extends UserDetailsService {
    UserDetails loadUserByUsernameAndGrantTokenAuthority(String username, TokenType tokenType);
}
