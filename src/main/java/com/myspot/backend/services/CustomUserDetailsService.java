package com.myspot.backend.services;

import com.myspot.backend.entities.PGManagementOwner;
import com.myspot.backend.repository.PGManagementRepository;
import com.myspot.backend.security.CustomUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final PGManagementRepository pgManagementRepository;
    
    @Override
    public UserDetails loadUserByUsername(String emailAddress) throws UsernameNotFoundException {
        log.debug("Loading PG Management user by email: {}", emailAddress);
        
        PGManagementOwner pgManagement = pgManagementRepository.findByEmailAddress(emailAddress)
                .orElseThrow(() -> {
                    log.error("PG Management not found with email: {}", emailAddress);
                    return new UsernameNotFoundException("PG Management not found with email: " + emailAddress);
                });
        
        log.debug("PG Management found: {}", pgManagement.getEmailAddress());
        
        return CustomUserPrincipal.create(pgManagement);
    }
    
    public UserDetails loadUserById(Long pgId) {
        log.debug("Loading PG Management user by ID: {}", pgId);
        
        PGManagementOwner pgManagement = pgManagementRepository.findById(pgId)
                .orElseThrow(() -> {
                    log.error("PG Management not found with ID: {}", pgId);
                    return new UsernameNotFoundException("PG Management not found with ID: " + pgId);
                });
        
        return CustomUserPrincipal.create(pgManagement);
    }
}