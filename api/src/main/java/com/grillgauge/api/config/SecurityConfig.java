package com.grillgauge.api.config;

import com.grillgauge.api.security.CertificateUserDetailsService;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Security configuration enabling X.509 (mTLS) authentication and CORS.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  static {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  public SecurityConfig() {
  }

  /**
   * Configure the security filter chain.
   */
  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http, CertificateUserDetailsService certificateUserDetailsService)
      throws Exception {

    http.cors(cors -> {
    });

    http.csrf(csrf -> csrf.disable());

    http.authorizeHttpRequests(auth -> auth
        .anyRequest().permitAll());

    http.x509(x509 -> x509.authenticationUserDetailsService(certificateUserDetailsService));

    return http.build();
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
