package me.probE466;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.hibernate.cfg.Configuration;


@SpringBootApplication
@EnableAutoConfiguration
public class PushApplication {

	public static void main(String[] args) {

		Configuration cfg = new Configuration();
		cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLInnoDBDialect");
		SpringApplication.run(PushApplication.class, args);
	}
}
