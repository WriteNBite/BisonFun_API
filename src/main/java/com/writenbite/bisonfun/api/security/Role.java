package com.writenbite.bisonfun.api.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ACCESS,
    ROLE_REFRESH;

    @Override
    public String getAuthority() {
        return name();
    }
}
