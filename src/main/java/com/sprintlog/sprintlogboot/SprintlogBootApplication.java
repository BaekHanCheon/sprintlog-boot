package com.sprintlog.sprintlogboot;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class SprintlogBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SprintlogBootApplication.class, args);
    }

}
