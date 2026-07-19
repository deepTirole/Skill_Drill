package com.deep.skill_drill.config;

import com.deep.skill_drill.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;

    public String parseTokenFromCookie(HttpServletRequest request) {
        if(request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt-token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authToken = parseTokenFromCookie(request);

        if(authToken == null || authToken.trim().isEmpty()) {
            filterChain.doFilter(request,response);
            return;
        }

        String username  = jwtService.extractUsername(authToken);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if(username != null && authentication == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if(jwtService.isTokenValid(authToken, userDetails)) {

                String role = jwtService.extractRole(authToken);

                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, authorities
                        );

                authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
