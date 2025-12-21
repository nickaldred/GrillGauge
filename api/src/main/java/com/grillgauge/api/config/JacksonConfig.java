package com.grillgauge.api.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration class for Jackson modules. */
@Configuration
public class JacksonConfig {

  /**
   * Bean configuration for Hibernate6Module to handle lazy loading.
   *
   * @return configured Hibernate6Module.
   */
  @Bean
  public Module hibernate6Module() {
    Hibernate6Module module = new Hibernate6Module();
    module.enable(Hibernate6Module.Feature.SERIALIZE_IDENTIFIER_FOR_LAZY_NOT_LOADED_OBJECTS);
    return module;
  }
}
