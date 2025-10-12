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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import pl.biketrack.properties.FrontendProperties;

import java.util.List;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.Customizer.withDefaults;
import static pl.biketrack.user.enumerated.Role.ADMIN;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String AUTH_ENDPOINT = "/api/v1/auth/**";

    private static final String[] WHITE_LIST_URL = {
            AUTH_ENDPOINT,
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final AuthenticationProvider authenticationProvider;
    private final LogoutService logoutService;
    private final FrontendProperties frontendProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(withDefaults())
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

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(frontendProperties.getBaseUrl()));
        config.setAllowedHeaders(List.of(ORIGIN, CONTENT_TYPE, ACCEPT, AUTHORIZATION));
        config.setAllowedMethods(List.of(GET.name(), POST.name(), DELETE.name(), PUT.name(), PATCH.name()));
        config.setExposedHeaders(List.of(CONTENT_DISPOSITION));
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}