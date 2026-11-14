package com.edward.chat_system;

import org.springframework.boot.SpringApplication;

public class TestChatSystemApplication {

    public static void main(String[] args) {
        SpringApplication.from(ChatSystemApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
