package com.grillgauge.api.config;

import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Security configuration for the API, setting up API key authentication and securing endpoints. */
@Configuration
public class SecurityConfig {
  static {
    if (Security.getProvider("BC") == null) {
      Security.addProvider(new BouncyCastleProvider());
    }
  }

  public SecurityConfig() {}

  /**
   * Configure the security filter chain.
   *
   * @param http the HttpSecurity to configure.
   * @return the SecurityFilterChain.
   * @throws Exception if an error occurs.
   */
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors() // ✅ enable CORS support
        .and()
        .csrf(csrf -> csrf.disable()); // disable CSRF for APIs

    return http.build();
  }

  /**
   * Configure CORS settings.
   *
   * @return the WebMvcConfigurer with CORS settings.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins("http://localhost:3000") // your React app origin
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
      }
    };
  }
}
