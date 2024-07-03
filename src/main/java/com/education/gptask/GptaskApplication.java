package com.education.gptask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableScheduling
@EnableFeignClients
public class GptaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(GptaskApplication.class, args);
	}

}
