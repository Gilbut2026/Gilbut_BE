package com.gilbeot.gilbut;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GilbutApplication {

	public static void main(String[] args) {
		SpringApplication.run(GilbutApplication.class, args);
	}

}
