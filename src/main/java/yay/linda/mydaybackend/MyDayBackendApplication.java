package yay.linda.mydaybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "yay.linda.mydaybackend.repository")
public class MyDayBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyDayBackendApplication.class, args);
	}

}
