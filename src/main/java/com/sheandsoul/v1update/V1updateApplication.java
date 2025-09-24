package com.sheandsoul.v1update;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class V1updateApplication {

	public static void main(String[] args) {
		Dotenv.load(); 
		SpringApplication.run(V1updateApplication.class, args);
	}

}
