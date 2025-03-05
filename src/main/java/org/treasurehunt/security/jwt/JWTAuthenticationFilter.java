package org.treasurehunt.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.treasurehunt.security.exception.TokenAuthenticationException;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.treasurehunt.common.constants.AuthConstants.BEARER_PREFIX;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthenticationEntryPoint authenticationEntryPoint;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authenticationHeader = request.getHeader(AUTHORIZATION);

        if (authenticationHeader == null) {
            filterChain.doFilter(request, response);
            return;
        }
        try {

            String jwtToken = removeBearerPrefix(authenticationHeader);
            UserDetails userDetails = jwtService.resolveJwtToken(jwtToken);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        } catch (AuthenticationException ex) {
            authenticationEntryPoint.commence(request, response, ex);
            return;
        }
        filterChain.doFilter(request, response);
    }

    String removeBearerPrefix(String token) {
        if (!token.startsWith(BEARER_PREFIX)) {
            throw new TokenAuthenticationException("Unsupported authentication scheme");
        }

        return token.substring(BEARER_PREFIX.length());
    }
}