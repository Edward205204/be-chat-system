package com.edward.chat_system;

import com.edward.chat_system.infrastructure.storage.StorageProperties;
import com.edward.chat_system.infrastructure.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableResilientMethods
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class ChatSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatSystemApplication.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return args -> storageService.init();
    }
}
