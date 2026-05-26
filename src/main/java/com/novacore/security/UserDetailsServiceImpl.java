package com.novacore.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.novacore.entity.User;
import com.novacore.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Not found: " + email));
        return org.springframework.security.core.userdetails.User.builder()
            .username(u.getEmail())
            .password("")
            .authorities(new SimpleGrantedAuthority(u.getRole()))
            .accountLocked(u.getIsBanned())
            .disabled(!u.getIsActive())
            .build();
    }
}
