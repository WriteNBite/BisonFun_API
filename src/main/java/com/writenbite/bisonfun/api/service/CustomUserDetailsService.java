package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.database.repository.UserRepository;
import com.writenbite.bisonfun.api.security.*;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Profile("prod")
@Component
public class CustomUserDetailsService implements TokenUserDetailsService {

    private final UserRepository userRepo;
    private final AuthorityMapper authorityMapper;

    public CustomUserDetailsService(UserRepository userRepo, AuthorityMapper authorityMapper) {
        this.userRepo = userRepo;
        this.authorityMapper = authorityMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optionalUser = userRepo.getByUsername(username);
        return optionalUser.map(UserPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));
    }

    @Override
    public UserDetails loadUserByUsernameAndGrantTokenAuthority(String username, TokenType tokenType) {
        Optional<User> optionalUser = userRepo.getByUsername(username);
        Role role = authorityMapper.fromTokenType(tokenType);
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (role != null) {
            authorities.add(role);
        }
        return optionalUser.map(user -> new UserPrincipal(user, authorities))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + username));
    }
}
