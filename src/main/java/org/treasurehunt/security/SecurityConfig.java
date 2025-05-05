package org.treasurehunt.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.treasurehunt.common.enums.Roles;
import org.treasurehunt.security.exception.ApplicationAuthenticationEntryPoint;
import org.treasurehunt.security.jwt.JWTAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JWTAuthenticationFilter jwtAuthenticationFilter;

    private final ApplicationAuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;


    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        return daoAuthenticationProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        final var authWhitelist = new String[]{
                "/api-docs/**", "/auth/refresh-token",
                "/auth/signin", "/auth/signup", "/swagger-ui/**",
                "/doc/**", "/index.html", "/error/**", "/users/**",
                "/hunts/*/images/bg", "/hunts/*/images/map"};


        http.
                csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(
                        configurer -> configurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(
                        customizer ->
                                customizer
                                        .accessDeniedHandler(accessDeniedHandler)
                                        .authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(
                        mather ->
                                mather
                                        .requestMatchers(authWhitelist)
                                        .permitAll()
                )
                .authorizeHttpRequests(
                        matcher ->
                                matcher.requestMatchers("/users/all").hasAuthority(Roles.ADMIN.name())
                                        .requestMatchers(HttpMethod.POST, "/products").hasAuthority(Roles.ADMIN.name())
                                        .requestMatchers(HttpMethod.PATCH, "/products").hasAuthority(Roles.ADMIN.name())
                                        .requestMatchers(HttpMethod.DELETE, "/products").hasAuthority(Roles.ADMIN.name())
                )
                .authorizeHttpRequests(
                        mather ->
                                mather.anyRequest().authenticated()
                );

        return http.build();
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}