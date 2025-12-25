package com.grillgauge.api.config;

import com.grillgauge.api.security.CertificateUserDetailsService;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Security configuration enabling X.509 (mTLS) authentication and CORS. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  static {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  @Value("${security.jwt.secret:}")
  private String jwtSecret;

  public SecurityConfig() {}

  /** Configure the security filter chain. */
  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, CertificateUserDetailsService certificateUserDetailsService)
      throws Exception {

    http.cors(cors -> {});

    http.csrf(csrf -> csrf.disable());

    http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.authorizeHttpRequests(
        auth ->
            auth
                // mTLS hubs
                .requestMatchers("/api/v1/externalHub/**")
                .hasRole("HUB")
                // UI aggregation endpoints
                .requestMatchers("/api/v1/ui/**")
                .authenticated()
                // User-owned resources
                .requestMatchers(
                    "/api/v1/hub/**",
                    "/api/v1/probe/**",
                    "/api/v1/register/confirm",
                    "/api/v1/register/*/revoke")
                .authenticated()
                // User management endpoints
                .requestMatchers("/api/v1/user/**")
                .authenticated()
                // Everything else (including /api/v1/user and hub device
                // registration/CSR) remains open for now.
                .requestMatchers("/api/v1/register/confirm", "/api/v1/register/revoke")
                .authenticated()
                .anyRequest()
                .permitAll());

    http.x509(x509 -> x509.authenticationUserDetailsService(certificateUserDetailsService));

    http.oauth2ResourceServer(
        oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  /**
   * Decoder for JWTs issued by the frontend authentication layer.
   *
   * <p>This expects HMAC-signed tokens using a shared secret configured via the {@code JWT_SECRET}
   * environment property.
   */
  @Bean
  JwtDecoder jwtDecoder() {
    if (jwtSecret == null || jwtSecret.isEmpty()) {
      // Fallback for local dev/tests; override in production via environment.
      // Must be at least 256 bits (32 bytes) for HS256.
      jwtSecret = "dev-test-jwt-secret-change-me-0123456789ABCDEF";
    }
    return NimbusJwtDecoder.withSecretKey(
            new javax.crypto.spec.SecretKeySpec(
                jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"))
        .build();
  }

  private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter =
        new JwtGrantedAuthoritiesConverter();
    grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

    JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
    authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    return authenticationConverter;
  }

  @Bean
  WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
      }
    };
  }
}
