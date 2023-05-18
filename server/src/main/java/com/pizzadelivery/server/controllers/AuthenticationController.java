package com.pizzadelivery.server.controllers;

import com.pizzadelivery.server.config.utils.JwtTokenUtil;
import com.pizzadelivery.server.config.utils.UserAuthorizationDetails;
import com.pizzadelivery.server.dto.AuthenticationDTO;
import com.pizzadelivery.server.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
//        (origins = "http://localhost:5173", allowCredentials = "true",
//        methods = {RequestMethod.POST, RequestMethod.OPTIONS},
//        exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"})
public class AuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @PostMapping(value = "/authenticate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> createAuthenticationToken(@RequestBody @Valid AuthenticationDTO request)
            throws Exception {
        try {
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword().trim()));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }

        final UserDetails userDetails = userService.loadUserByUsername(request.getEmail());
        final String token = jwtTokenUtil.generateToken(userDetails);
        final String domain = "localhost";
        final long maxAge = JwtTokenUtil.JWT_TOKEN_VALIDITY;
        final String path = "/";
        final String id = String.valueOf(((UserAuthorizationDetails) userDetails).getId());

        if (request.isRememberMe()) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Set-Cookie",
                    String.format("jwt=%s; Domain=%s; Max-Age=%d; Path=%s; SameSite=None; Secure;",
                            token, domain, maxAge, path));
            return ResponseEntity.status(HttpStatus.OK).headers(headers).body(Collections.singletonMap("id", id));
        } else {
            var map = new HashMap<String, String>();
            map.put("token", token);
            map.put("id", id);
            return ResponseEntity.ok(map);
        }
    }

    @GetMapping(value = "/unset")
    public ResponseEntity<?> unsetTokenCookie(HttpServletRequest request) {
        final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String domain = "localhost";
        final int maxAge = 0;
        final String path = "/";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Set-Cookie",
                String.format("jwt=%s; Domain=%s; Max-Age=%d; Path=%s; SameSite=None; Secure;",
                        token, domain, maxAge, path));
        return ResponseEntity.status(HttpStatus.OK).headers(headers).build();
    }
}
