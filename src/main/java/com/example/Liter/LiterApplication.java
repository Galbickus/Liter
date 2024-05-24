package com.example.Liter;

import com.example.Liter.service.ConsumoAPI;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiterApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(LiterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		var consumoApi = new ConsumoAPI();
		var json = consumoApi.obtenerDatos("https://gutendex.com/books/");
		System.out.println(json);
	}
}
