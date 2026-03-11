package com.aumReport.main;

import com.aumReport.aum.security.AuthSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication(scanBasePackages = "com.aumReport")
@EnableJpaRepositories(basePackages = "com.aumReport.aum.repo")
@EntityScan(basePackages = "com.aumReport.aum.entity")
@EnableConfigurationProperties(AuthSecurityProperties.class)
public class AumApplication extends SpringBootServletInitializer{

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(AumApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(AumApplication.class, args);
	}

}
