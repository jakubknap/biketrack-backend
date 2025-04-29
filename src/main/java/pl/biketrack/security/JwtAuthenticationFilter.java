package pl.biketrack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.biketrack.security.util.SecurityUtils;
import pl.biketrack.token.enumerated.TokenType;

import java.io.IOException;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String jwt = jwtService.readJwtFromHeader(request);

        if (isNull(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String userEmail = jwtService.extractUsername(jwt);

        if (nonNull(userEmail) && isNull(SecurityUtils.getAuthentication())) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails.getUsername(), TokenType.ACCESS_TOKEN)) {
                setAuthentication(request, userDetails);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails,
                                                                                            null,
                                                                                            userDetails.getAuthorities());
        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext()
                             .setAuthentication(token);
    }
}