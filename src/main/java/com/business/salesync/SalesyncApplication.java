package com.business.salesync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication(scanBasePackages = "com.business.salesync")
@EntityScan(basePackages = "com.business.salesync.models")
@EnableJpaRepositories(basePackages = "com.business.salesync.repository")
public class SalesyncApplication {

	public static void main(String[] args) {
		SpringApplication.run(SalesyncApplication.class, args);
	}

}
