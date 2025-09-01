package com.myspot.backend.security;

import com.myspot.backend.entities.PGManagementOwner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails {
    
    private final PGManagementOwner pgManagementOwner;
    
    public CustomUserPrincipal(PGManagementOwner pgManagementOwner) {
        this.pgManagementOwner = pgManagementOwner;
    }
    
    public static CustomUserPrincipal create(PGManagementOwner pgManagementOwner) {
        return new CustomUserPrincipal(pgManagementOwner);
    }
    
    public Long getId() {
        return pgManagementOwner.getPgId();
    }
    
    public PGManagementOwner getPgManagementOwner() {
        return pgManagementOwner;
    }
    
    // ADD: This fixes the JWT error!
    public String getEmail() {
        return pgManagementOwner.getEmailAddress();
    }
    
    @Override
    public String getUsername() {
        return pgManagementOwner.getEmailAddress();
    }
    
    @Override
    public String getPassword() {
        return pgManagementOwner.getPasswordHash();
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_PG_OWNER"));
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return pgManagementOwner.getIsActive();
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return pgManagementOwner.getIsActive() && pgManagementOwner.getEmailVerified();
    }
}