package io.lrsystem.ServiceLog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ServiceLogApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServiceLogApplication.class, args);
	}

}
