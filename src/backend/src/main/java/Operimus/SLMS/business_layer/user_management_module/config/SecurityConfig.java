package Operimus.SLMS.business_layer.user_management_module.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${JWT_ISSUER_URI}")
    private String externalIssuerUri;

    @Value("${KEYCLOAK_HOST}")
    private String keycloakHost;

    @Value("${KEYCLOAK_PORT}")
    private String keycloakPort;

    @Value("${KEYCLOAK_REALM}")
    private String keycloakRealm;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable()) // Allow CORS for development
            .authorizeHttpRequests(auth -> auth
                // BFF Authentication endpoints (public)
                .requestMatchers("/api/landing-page/auth/**").permitAll()
                .requestMatchers("/api/landing-page/chat").permitAll() // Public chatbot for landing page
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
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * Custom JWT decoder to handle issuer validation in Docker environment
     *
     * Problem: Keycloak can issue tokens with different issuer claims depending on how it's accessed:
     * - Direct access: http://localhost:8180/realms/slms
     * - Through nginx proxy: http://localhost:8080/realms/slms or http://localhost/realms/slms
     * - Internal Docker network: http://keycloak:8080/realms/slms
     *
     * Solution: Configure decoder to:
     * 1. Fetch JWK keys from internal Docker network (keycloak:8080)
     * 2. Accept tokens with ANY of the possible issuer variations
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Use NimbusJwtDecoder with JWK Set URI (internal Docker network)
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        // Build list of all possible valid issuers
        String internalIssuerUri = String.format("http://%s:%s/realms/%s", keycloakHost, keycloakPort, keycloakRealm);
        String nginxIssuerWithPort = String.format("http://localhost:%s/realms/%s", keycloakPort, keycloakRealm); // http://localhost:8080/realms/slms
        String nginxIssuerNoPort = String.format("http://localhost/realms/%s", keycloakRealm); // http://localhost/realms/slms

        List<String> validIssuers = List.of(
            externalIssuerUri,      // http://localhost:8180/realms/slms (direct external access)
            internalIssuerUri,      // http://keycloak:8080/realms/slms (internal Docker network)
            nginxIssuerWithPort,    // http://localhost:8080/realms/slms (nginx proxy with internal port)
            nginxIssuerNoPort       // http://localhost/realms/slms (nginx proxy without port)
        );

        // Configure validators to accept ANY of the valid issuers
        OAuth2TokenValidator<Jwt> withIssuer = new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            token -> {
                String issuer = token.getIssuer() != null ? token.getIssuer().toString() : null;
                if (validIssuers.contains(issuer)) {
                    return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success();
                }
                return org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.failure(
                    new org.springframework.security.oauth2.core.OAuth2Error(
                        "invalid_token",
                        String.format("Invalid issuer: %s. Expected one of: %s", issuer, String.join(", ", validIssuers)),
                        null
                    )
                );
            }
        );

        jwtDecoder.setJwtValidator(withIssuer);
        return jwtDecoder;
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
