package org.example.gateway.config;

import org.example.gateway.security.AuthorityConstant;
import org.example.gateway.security.jwt.TokenProvider;
import org.example.gateway.security.jwt.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final TokenProvider tokenProvider;

    public SecurityConfig( TokenProvider tokenProvider ) {
        this.tokenProvider = tokenProvider;
    }

    @Bean // Indica que es un componente de spring
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain( final HttpSecurity http ) throws Exception {
        http
                .csrf( AbstractHttpConfigurer::disable );
        http
                .sessionManagement( s -> s.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) );
        http
                .securityMatcher("/api/**" )
                .authorizeHttpRequests( authz -> authz
                        //mas especificos
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/eventos").hasAuthority(AuthorityConstant.ORGANIZADOR)
                        .requestMatchers(HttpMethod.POST, "/api/eventos/lugares").hasAuthority(AuthorityConstant.ORGANIZADOR)
                        .requestMatchers(HttpMethod.POST, "/api/entradas/revision").hasAuthority(AuthorityConstant.ADMINISTRADOR)
                        .requestMatchers(HttpMethod.PATCH, "/api/eventos/{id}/fecha").hasAuthority(AuthorityConstant.ORGANIZADOR)
                        .requestMatchers(HttpMethod.PATCH, "/api/eventos/{id}/estado").hasAnyAuthority(AuthorityConstant.ADMINISTRADOR, AuthorityConstant.ORGANIZADOR)
                        .requestMatchers(HttpMethod.PATCH, "/api/entradas/validar").hasAuthority(AuthorityConstant.PERSONAL_DE_ACCESO)
                        .requestMatchers(HttpMethod.GET, "/api/entradas").hasAuthority(AuthorityConstant.CLIENTE)
                        .requestMatchers(HttpMethod.GET, "/api/ordenes/reportes").hasAuthority(AuthorityConstant.ORGANIZADOR)
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/eventos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/eventos/lugares/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ordenes/**").permitAll()
                        //menos especificos
                        .requestMatchers("/api/ordenes/**").hasAuthority(AuthorityConstant.CLIENTE)
                        .requestMatchers("/api/pagos/**").hasAuthority(AuthorityConstant.CLIENTE)
                        .requestMatchers("/api/reservas/**").hasAuthority(AuthorityConstant.CLIENTE)
                        .requestMatchers("/api/notificaciones/**").permitAll()
                        .requestMatchers("api/usuarios/**").permitAll()



                        .anyRequest().authenticated()
                )
                .httpBasic( Customizer.withDefaults() )
                .addFilterBefore( new JwtFilter( this.tokenProvider ), UsernamePasswordAuthenticationFilter.class );
        return http.build();
    }


}