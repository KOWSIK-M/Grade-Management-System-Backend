package com.klef.gms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GradeManagementSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(GradeManagementSystemApplication.class, args);
	}

}
