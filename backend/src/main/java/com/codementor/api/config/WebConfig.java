package com.codementor.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final RateLimitInterceptor rateLimit;

  public WebConfig(RateLimitInterceptor rateLimit) {
    this.rateLimit = rateLimit;
  }

  @Bean
  public RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(rateLimit);
  }
}
