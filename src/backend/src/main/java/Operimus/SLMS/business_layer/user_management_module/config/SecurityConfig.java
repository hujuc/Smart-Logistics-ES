package Operimus.SLMS.business_layer.user_management_module.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Security configuration for SLMS backend
     *
     * Architecture:
     * 1. BFF Layer (Role-Based Access Control):
     *    - /api/logistics/** - Logistics Manager access
     *    - /api/warehouse/** - Warehouse Staff access
     *    - /api/driver/** - Driver access
     *    - /api/customer-service/** - Customer Service access
     *    - /api/customer/** - Customer access
     *
     * 2. Domain Layer (Internal Development APIs - No Auth):
     *    - /api/admin/** - Direct access to domain controllers
     *    - Used for development/testing without role restrictions
     *
     * 3. Public Endpoints:
     *    - /api/landing-page/auth/** - Authentication endpoints
     *    - /api/public/** - Public resources
     */
    
    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain groqSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/groq/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        return http.build();
    }
    
    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable()) // Allow CORS for development
            .authorizeHttpRequests(auth -> auth
                // BFF Authentication endpoints (public)
                .requestMatchers("/api/landing-page/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Internal Development APIs - Domain Layer Controllers (no auth required)
                .requestMatchers("/api/admin/**").permitAll() // Catch-all for other domain controllers
                // Protected BFF endpoints (role-based)
                .requestMatchers("/api/logistics/**").hasRole("LOGISTICS_MANAGER")
                .requestMatchers("/api/warehouse/**").hasRole("WAREHOUSE_STAFF")
                .requestMatchers("/api/driver/**").hasRole("DRIVER")
                .requestMatchers("/api/customer-service/**").hasRole("CUSTOMER_SERVICE_REP")
                .requestMatchers("/api/customer/**").hasRole("CUSTOMER")
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extract roles from nested "realm_access.roles" claim
            var realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess == null || !realmAccess.containsKey("roles")) {
                return java.util.Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            var roles = (java.util.List<String>) realmAccess.get("roles");

            return roles.stream()
                .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                .collect(java.util.stream.Collectors.toList());
        });
        return jwtAuthenticationConverter;
    }
}
