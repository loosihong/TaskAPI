package com.example.TaskAPI.auth.service.impl;

import com.example.TaskAPI.auth.api.AuthResponse;
import com.example.TaskAPI.auth.service.AuthService;
import com.example.TaskAPI.core.exception.DuplicateEntityException;
import com.example.TaskAPI.infrastructure.security.CustomUserDetails;
import com.example.TaskAPI.infrastructure.security.CustomUserDetailsService;
import com.example.TaskAPI.security.JwtIssuer;
import com.example.TaskAPI.user.domain.entity.User;
import com.example.TaskAPI.user.domain.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    public AuthServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtIssuer jwtIssuer,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtIssuer = jwtIssuer;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
    }

    public User register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new DuplicateEntityException("Username already taken");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        return userRepository.save(user);
    }

    public AuthResponse login(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);
        String token = jwtIssuer.issue(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList());

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}
