package Operimus.SLMS.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Test configuration that provides a mock JwtDecoder to avoid connecting to Keycloak during tests.
 *
 * This configuration should be used in integration tests that need to test the full Spring Security
 * configuration but don't want to connect to an actual Keycloak instance.
 *
 * Usage:
 * - For @SpringBootTest or @WebMvcTest that loads SecurityConfig, import this with @Import(TestSecurityConfig.class)
 * - For @WebMvcTest that doesn't need security, use @AutoConfigureMockMvc(addFilters = false) instead
 */
@TestConfiguration
public class TestSecurityConfig {

    /**
     * Provides a mock JwtDecoder that creates a simple JWT token without validation.
     * This allows tests to bypass Keycloak authentication while still testing security rules.
     */
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return token -> {
            Map<String, Object> headers = new HashMap<>();
            headers.put("alg", "none");

            Map<String, Object> claims = new HashMap<>();
            claims.put("sub", "test-user");
            claims.put("iat", Instant.now().getEpochSecond());
            claims.put("exp", Instant.now().plusSeconds(3600).getEpochSecond());

            return new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), headers, claims);
        };
    }
}
