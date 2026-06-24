package com.ayman.configlib.response;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ResponseCodeProperties.class)
public class ResponseHandlerAutoConfiguration {
}
