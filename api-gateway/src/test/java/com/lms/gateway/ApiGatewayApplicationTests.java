package com.lms.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test confirming the reactive application context loads with the
 * configured routes, JWT filter, and properties bound correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
        // If the Spring context fails to start (bad route config, missing
        // beans, malformed properties) this test fails.
    }
}
