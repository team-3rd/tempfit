package com.example.tempfit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class TempFitApplication {

	public static void main(String[] args) {
		SpringApplication.run(TempFitApplication.class, args);
	}

}
