package com.edward.chat_system;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Disabled("Tạm thời disable, cần setup db.")
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class ChatSystemApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context loads successfully without errors.
        // No additional assertions needed — a failed context startup will throw an exception.
    }
}
