package com.project.hotelbooking.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_USER,
    ROLE_VENDOR,
    ROLE_ADMIN;

    @Override
    public String getAuthority() {
        return name();
    }
}