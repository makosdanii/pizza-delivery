package com.pizzadelivery.server.config;

import com.pizzadelivery.server.config.filter.CorsRequestFilter;
import com.pizzadelivery.server.config.filter.JwtEntryPoint;
import com.pizzadelivery.server.config.filter.JwtRequestFilter;
import com.pizzadelivery.server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.security.SecureRandom;
import java.util.HexFormat;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class BasicConfiguration {

    private final UserService userDetailsService;
    private final JwtEntryPoint jwtEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final CorsRequestFilter corsRequestFilter;

    @Autowired
    public BasicConfiguration(@Lazy UserService userDetailsService,
                              JwtEntryPoint jwtEntryPoint,
                              JwtRequestFilter jwtRequestFilter,
                              CorsRequestFilter corsRequestFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtEntryPoint = jwtEntryPoint;
        this.jwtRequestFilter = jwtRequestFilter;
        this.corsRequestFilter = corsRequestFilter;
    }

    @Value("${salt}")
    private String salt;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(11, new SecureRandom(HexFormat.of().parseHex(salt)));
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(encoder());

        return authProvider;
    }

    private static final RequestMatcher[] PUBLIC_URLS = {
            new AntPathRequestMatcher("/user/register"),
            new AntPathRequestMatcher("/authenticate"),
            new AntPathRequestMatcher("/menu"),
            new AntPathRequestMatcher("/street"),
            new AntPathRequestMatcher("/delivery/**"),
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(corsRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http
                .cors()
                .and()
                .csrf().disable()
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(
                        authorize -> authorize
                                .requestMatchers(PUBLIC_URLS).permitAll()
                                .anyRequest().authenticated()
                )
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtEntryPoint)
                .and()
                .httpBasic().and()
                .build();
    }
}
