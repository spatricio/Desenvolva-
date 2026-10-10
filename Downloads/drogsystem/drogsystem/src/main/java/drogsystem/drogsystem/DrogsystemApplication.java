package drogsystem.drogsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DrogsystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrogsystemApplication.class, args);
	}

}
