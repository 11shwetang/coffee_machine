package com.coffee.machine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.inventory", "com.beverages"})
public class CoffeeMachineApplication {
	public static void main(String[] args) {
		SpringApplication.run(CoffeeMachineApplication.class, args);
	}
}
