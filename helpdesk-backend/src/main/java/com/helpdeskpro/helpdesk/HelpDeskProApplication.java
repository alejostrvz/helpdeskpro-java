package com.helpdeskpro.helpdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class HelpDeskProApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelpDeskProApplication.class, args);
	}

}
