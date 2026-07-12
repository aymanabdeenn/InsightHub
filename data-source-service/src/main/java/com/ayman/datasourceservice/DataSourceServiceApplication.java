package com.ayman.datasourceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
@ComponentScan(basePackages = {"com.ayman.datasourceservice", "com.ayman.configlib"})
public class DataSourceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataSourceServiceApplication.class, args);
	}

}
