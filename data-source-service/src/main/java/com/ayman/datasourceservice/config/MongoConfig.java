package com.ayman.datasourceservice.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;


@Configuration
public class MongoConfig {
    // WORKAROUND for a Spring Boot 4.0.6 binding bug: MongoAutoConfiguration's
    // MongoProperties fails to bind spring.data.mongodb.uri via relaxed-binding,
    // regardless of source, so the auto-configured MongoClient/MongoDatabaseFactory
    // always fall back to mongodb://localhost/test.
    //
    // Building both MongoClient and MongoDatabaseFactory ourselves, from the same
    // @Value-resolved URI (a direct key lookup, which works reliably).
    //
    // Important: MongoAutoConfiguration is annotated
    // @ConditionalOnMissingBean(MongoDatabaseFactory.class) at the CLASS level —
    // defining our own MongoDatabaseFactory disables the ENTIRE auto-configuration
    // class, including its MongoClient bean method. So we must build MongoClient
    // ourselves too, rather than relying on Boot to supply it from a
    // MongoClientSettings bean alone — that mechanism only works if
    // MongoAutoConfiguration is still active, which it no longer is once any
    // MongoDatabaseFactory bean is present.
    @Bean
    public MongoClient mongoClient(@Value("${spring.data.mongodb.uri}") String uri) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .build();
        return MongoClients.create(settings);
    }

    @Bean
    public MongoDatabaseFactory mongoDatabaseFactory(MongoClient mongoClient, @Value("${spring.data.mongodb.uri}") String uri) {
        String databaseName = new ConnectionString(uri).getDatabase();
        return new SimpleMongoClientDatabaseFactory(mongoClient, databaseName);
    }
}
