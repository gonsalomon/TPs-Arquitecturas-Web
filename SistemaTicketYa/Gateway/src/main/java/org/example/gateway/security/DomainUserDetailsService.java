package org.example.gateway.security;


import lombok.AllArgsConstructor;
import org.example.gateway.client.UsuarioClient;
import org.example.gateway.client.dto.user.AuthorityResponseDTO;
import org.example.gateway.client.dto.user.UsuarioTokenResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);
    private final UsuarioClient usuarioClient;


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(final String username) {
        log.debug("Authenticating {}", username);

        return usuarioClient
                .findOneWithAuthoritiesByEmailIgnoreCase(username.toLowerCase())
                .map(this::createSpringSecurityUser)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe"));
    }


    private UserDetails createSpringSecurityUser(UsuarioTokenResponseDTO user) {
        List<GrantedAuthority> grantedAuthorities = user
                .getRoles()
                .stream()
                .map(AuthorityResponseDTO::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(user.getUsuario(), user.getPassword(), grantedAuthorities);
    }
}