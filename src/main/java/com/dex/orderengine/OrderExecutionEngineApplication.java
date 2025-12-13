package com.dex.orderengine;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OrderExecutionEngineApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		System.setProperty("PGHOST", dotenv.get("PGHOST"));
		System.setProperty("PGDATABASE", dotenv.get("PGDATABASE"));
		System.setProperty("PGUSER", dotenv.get("PGUSER"));
		System.setProperty("PGPASSWORD", dotenv.get("PGPASSWORD"));
		SpringApplication.run(OrderExecutionEngineApplication.class, args);
	}

}
