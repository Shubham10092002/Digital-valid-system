// src/main/java/com/example/digitalWalletDemo/DigitalWalletDemoApplication.java
// (Modified - Add @Bean method)

package com.example.digitalWalletDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;


@SpringBootApplication

public class DigitalWalletDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalWalletDemoApplication.class, args);
    }

    // Demonstrates a custom Spring Bean using @Bean and accessing environment variables
    @Bean
    public String applicationStartupMessage(Environment env) {
        String name = env.getProperty("spring.application.name");
        String message = env.getProperty("app.message");
        System.out.println("--- Application Context Initialized ---");
        System.out.println("Application Name: " + name);
        System.out.println("Config Message: " + message);
        return message;
    }
}