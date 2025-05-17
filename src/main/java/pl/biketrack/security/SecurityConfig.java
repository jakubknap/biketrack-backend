package pl.biketrack.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static pl.biketrack.user.enumerated.Role.ADMIN;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String AUTH_ENDPOINTS = "/api/v1/auth/**";
    private static final String SWAGGER_CONFIG_ENDPOINTS = "/v3/api-docs/**";
    private static final String SWAGGER_ENDPOINTS = "/swagger-ui/**";

    private static final String[] WHITE_LIST_URL = {
            AUTH_ENDPOINTS,
            SWAGGER_CONFIG_ENDPOINTS,
            SWAGGER_ENDPOINTS
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutService logoutService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authRequests -> authRequests.requestMatchers(WHITE_LIST_URL).permitAll()
                                                               .requestMatchers("/admin/**").hasRole(ADMIN.name())
                                                               .anyRequest().authenticated())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .logout(logout -> logout.logoutUrl("/api/v1/auth/logout")
                                    .addLogoutHandler(logoutService)
                                    .invalidateHttpSession(true)
                                    .clearAuthentication(true)
                                    .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
                                    .permitAll());

        return http.build();
    }
}