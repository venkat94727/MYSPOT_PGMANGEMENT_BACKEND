
package com.myspot.backend.security;

import com.myspot.backend.entities.PGManagementOwner;
import com.myspot.backend.repository.PGManagementOwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PGManagementOwnerRepository pgManagementOwnerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        PGManagementOwner pgManagementOwner = pgManagementOwnerRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UsernameNotFoundException("PG not found with email: " + email));

        return new CustomUserPrincipal(pgManagementOwner);
    }
}