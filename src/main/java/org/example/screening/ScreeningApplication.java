package org.example.screening;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ScreeningApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScreeningApplication.class, args);
    }

}
