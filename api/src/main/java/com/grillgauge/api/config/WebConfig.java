package com.grillgauge.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// import com.grillgauge.api.domain.converters.ProbeReadingToReading;

/** Web configuration for the API. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  // @Override
  // public void addFormatters(@NonNull final FormatterRegistry registry) {
  // registry.addConverter(new ProbeReadingToReading());
  // }
}
