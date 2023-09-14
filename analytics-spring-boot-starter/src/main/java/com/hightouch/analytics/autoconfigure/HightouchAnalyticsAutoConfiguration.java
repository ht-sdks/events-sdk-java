package com.hightouch.analytics.autoconfigure;

import com.hightouch.analytics.Analytics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot autoconfiguration class for Hightouch Analytics.
 *
 * @author Christopher Smith
 */
@Configuration
@EnableConfigurationProperties(HightouchProperties.class)
@ConditionalOnProperty("hightouch.analytics.writeKey")
public class HightouchAnalyticsAutoConfiguration {

  @Autowired private HightouchProperties properties;

  @Bean
  public Analytics hightouchAnalytics() {
    return Analytics.builder(properties.getWriteKey()).build();
  }
}
