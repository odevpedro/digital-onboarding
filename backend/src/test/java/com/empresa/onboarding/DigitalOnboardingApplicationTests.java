package com.empresa.onboarding;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires Docker for TestContainers")
class DigitalOnboardingApplicationTests {

    @Test
    void contextLoads() {
    }
}
