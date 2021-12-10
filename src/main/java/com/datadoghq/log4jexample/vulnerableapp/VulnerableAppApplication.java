package com.datadoghq.log4jexample.vulnerableapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VulnerableAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(VulnerableAppApplication.class, args);
	}

}
