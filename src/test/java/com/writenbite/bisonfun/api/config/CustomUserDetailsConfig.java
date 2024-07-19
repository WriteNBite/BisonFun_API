package com.writenbite.bisonfun.api.config;

import com.writenbite.bisonfun.api.InMemoryTokenUserDetailsManager;
import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.security.Role;
import com.writenbite.bisonfun.api.service.TokenUserDetailsService;
import com.writenbite.bisonfun.api.security.UserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Collections;

@Configuration
public class CustomUserDetailsConfig {
    @Bean
    @Profile("test")
    public TokenUserDetailsService inMemoryDetailsService() {
        User accessUser = new User();
        accessUser.setId(1);
        accessUser.setPassword("pass");
        accessUser.setEmail("access@test.com");
        accessUser.setUsername("AUser");

        User refreshUser = new User();
        refreshUser.setId(2);
        refreshUser.setPassword("pass");
        refreshUser.setEmail("refresh@test.com");
        refreshUser.setUsername("RUser");

        User noTokenUser = new User();
        noTokenUser.setId(3);
        noTokenUser.setPassword("pass");
        noTokenUser.setEmail("notoken@test.com");
        noTokenUser.setUsername("NoToke");

        return new InMemoryTokenUserDetailsManager(new InMemoryUserDetailsManager(
                new UserPrincipal(accessUser, Collections.singleton(Role.ROLE_ACCESS)),
                new UserPrincipal(refreshUser, Collections.singleton(Role.ROLE_REFRESH)),
                new UserPrincipal(noTokenUser)
        ));
    }
}
