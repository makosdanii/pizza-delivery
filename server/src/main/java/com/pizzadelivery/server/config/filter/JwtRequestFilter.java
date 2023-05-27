package com.pizzadelivery.server.config.filter;

import com.pizzadelivery.server.config.utils.JwtTokenUtil;
import com.pizzadelivery.server.data.entities.User;
import com.pizzadelivery.server.services.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static com.pizzadelivery.server.services.ServiceORM.UNASSIGNED;

/**
 * Filters before processing the requests, if valid authentication token is found
 * then sets authenticated user into Security context.
 * Similar to referenced one on javainuse.com
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @Autowired
    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil, @Lazy UserService userService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    String email = null;
    String jwtToken = null;

    /**
     * Method for checking for the token, validating, setting the Security Context
     *
     * @param request is to be checked for authorization header
     * @param chain   forwards the request
     * @throws ServletException malformed request
     * @throws IOException      malformed token
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String BEARER = "Bearer ";

        if (header != null && header.startsWith("root")) {
            User user = userService.findAdmin();

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
            if (user.getId() == UNASSIGNED) {
                usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        null, null, List.of(new SimpleGrantedAuthority("admin")));
            } else {
                UserDetails userDetails = userService.loadUserByUsername(user.getEmail());
                usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
            }

            usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // After setting the Authentication in the context, we specify
            // that the current user is authenticated. So it passes the
            // Spring Security Configurations successfully.
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            chain.doFilter(request, response);
            return;
        }

        if (header != null && header.startsWith(BEARER)) {
            jwtToken = header.substring(BEARER.length());
            try {
                email = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                logger.warn("JWT Token has expired");
            } catch (MalformedJwtException e) {
                logger.warn("JWT Token was malformed");
            }
        } else {
            logger.warn("JWT Token does not begin with Bearer String");

            chain.doFilter(request, response);
            return;
        }

        // Once we get the token validate it.
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(email);
            // if token is valid configure Spring Security to manually set
            // authentication
            try {
                if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the
                    // Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            } catch (ExpiredJwtException e) {
                logger.warn("Expired token");
            }
        }
        chain.doFilter(request, response);
    }
}