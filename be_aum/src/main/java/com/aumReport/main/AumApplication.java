package com.aumReport.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.aumReport")
@EnableJpaRepositories(basePackages = "com.aumReport.aum.repo")
@EntityScan(basePackages = "com.aumReport.aum.entity")
public class AumApplication {

	public static void main(String[] args) {
		SpringApplication.run(AumApplication.class, args);
	}

}
