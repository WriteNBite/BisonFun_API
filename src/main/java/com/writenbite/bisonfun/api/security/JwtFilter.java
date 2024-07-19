package com.writenbite.bisonfun.api.security;

import com.writenbite.bisonfun.api.service.JwtService;
import com.writenbite.bisonfun.api.service.TokenUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final TokenUserDetailsService userDetailsService;
    private final AuthorityMapper authorityMapper;

    public JwtFilter(JwtService jwtService, TokenUserDetailsService userDetailsService, AuthorityMapper authorityMapper) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.authorityMapper = authorityMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Extracting token from the request header
        String authHeader = request.getHeader("Authorization");
        String token;
        String username = null;
        JwtTokenHolder tokenHolder = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extracting the token from the Authorization header
            token = authHeader.substring(7);
            // Extracting username from the token
            try {
                tokenHolder = jwtService.getHolder(token);
                username = tokenHolder.extractUserName();
            } catch (TokenExpiredException | TokenValidationException e) {
                log.warn(e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            }
        }

        // If username is extracted and there is no authentication in the current SecurityContext and the token with proper type
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Loading UserDetails by username extracted from the token
            UserDetails userDetails = userDetailsService.loadUserByUsernameAndGrantTokenAuthority(username, tokenHolder.extractTokenType());

            // Validation the token with loaded UserDetails
            if (tokenHolder.validate(userDetails, authorityMapper)) {
                // Creating an authentication token using UserDetails
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // Setting authentication details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Setting the authentication token in the SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Proceeding with the filter chain
        filterChain.doFilter(request, response);
    }
}
