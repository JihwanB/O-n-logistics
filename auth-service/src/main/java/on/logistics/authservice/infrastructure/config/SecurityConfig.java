package on.logistics.authservice.infrastructure.config;

import lombok.RequiredArgsConstructor;
import on.logistics.authservice.application.PassportService;
import on.logistics.authservice.infrastructure.security.cookie.CookieUtil;
import on.logistics.authservice.infrastructure.security.details.AuthDetailsServiceImpl;
import on.logistics.authservice.infrastructure.security.jwt.JwtAuthenticationFilter;
import on.logistics.authservice.infrastructure.security.jwt.JwtAuthorizationFilter;
import on.logistics.authservice.infrastructure.security.jwt.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final AuthDetailsServiceImpl authDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final PassportService passportService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        AuthenticationConfiguration authenticationConfiguration
    ) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthentiactionFilter() throws Exception {
        return JwtAuthenticationFilter.builder()
            .jwtUtil(jwtUtil)
            .cookieUtil(cookieUtil)
            .authenticationManager(authenticationConfiguration.getAuthenticationManager())
            .passportService(passportService)
            .build();
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() throws Exception {
        return JwtAuthorizationFilter.builder()
            .jwtUtil(jwtUtil)
            .cookieUtil(cookieUtil)
            .authDetailsService(authDetailsService)
            .passportService(passportService)
            .build();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(
                session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                .requestMatchers(
                    "/",
                    "/api/v1/auth/signup",
                    "/api/v1/auth/login"
                ).permitAll()
                .requestMatchers(
                    "/actuator/prometheus",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                .requestMatchers(
                    "/docs/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthentiactionFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class);

        return http.build();
    }

}
