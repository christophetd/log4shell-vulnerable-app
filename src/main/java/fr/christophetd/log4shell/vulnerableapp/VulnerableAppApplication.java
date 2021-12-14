package fr.christophetd.log4shell.vulnerableapp;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class VulnerableAppApplication {

	public static void main(String[] args) {
    BasicConfigurator.configure();
    SpringApplication.run(VulnerableAppApplication.class, args);
	}

}
