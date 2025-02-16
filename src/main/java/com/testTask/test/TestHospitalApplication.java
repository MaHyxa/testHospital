package com.testTask.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TestHospitalApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestHospitalApplication.class, args);
	}

}
