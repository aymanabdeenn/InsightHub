package com.ayman.datasourceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class DataSourceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataSourceServiceApplication.class, args);
	}

}
