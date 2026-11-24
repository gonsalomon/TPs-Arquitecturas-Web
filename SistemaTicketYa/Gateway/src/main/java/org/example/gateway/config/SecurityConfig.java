package org.example.gateway.config;

import org.example.gateway.security.AuthorityConstant;
import org.example.gateway.security.jwt.JwtFilter;
import org.example.gateway.security.jwt.TokenProvider;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final TokenProvider tokenProvider;

    public SecurityConfig(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authz -> authz
                        // Preflight CORS del navegador
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Más específicos
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
                        // Menos específicos
                        .requestMatchers("/api/ordenes/**").hasAuthority(AuthorityConstant.CLIENTE)
                        .requestMatchers("/api/pagos/**").hasAuthority(AuthorityConstant.CLIENTE)
                        .requestMatchers("/api/reservas/**").hasAuthority(AuthorityConstant.CLIENTE)
                        .requestMatchers("/api/fila/**").hasAuthority(AuthorityConstant.CLIENTE)
                        .requestMatchers("/api/notificaciones/**").permitAll()
                        .requestMatchers("/api/usuarios/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(new JwtFilter(this.tokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}