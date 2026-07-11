package com.ayman.datasourceservice.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MongoConfig {
    // WORKAROUND for a Spring Boot 4.0.6 binding bug: MongoAutoConfiguration's
    // MongoProperties (used to auto-build the MongoClient) fails to bind
    // spring.data.mongodb.uri via relaxed-binding / @ConfigurationProperties,
    // regardless of source — confirmed with both a Spring Cloud Config Server
    // value AND a hardcoded local application.yaml value. In both cases the
    // property IS present in the Environment (confirmed via
    // /actuator/env/spring.data.mongodb.uri) but MongoProperties.uri stays null,
    // and Boot silently falls back to mongodb://localhost/test.
    //
    // Providing our own MongoClientSettings bean bypasses MongoProperties
    // entirely (Spring backs off auto-configuring one if we supply it),
    // resolving the URI via @Value (direct key lookup) instead, which works
    // correctly regardless of source.
    //
    // Safe to remove and re-test once upgrading past this Spring Boot version —
    // a similar pattern (imported source specifically) is reported at
    // https://github.com/GoogleCloudPlatform/spring-cloud-gcp/issues/4395,
    // though our case shows the underlying binding failure is broader than
    // that issue describes.
    @Bean
    public MongoClientSettings mongoClientSettings(@Value("${spring.data.mongodb.uri}") String uri) {
        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .build();
    }
}
