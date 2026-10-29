package com.lms.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Single entry point for the LMS microservices. Routes incoming HTTP requests to
 * auth-service, student-service, course-service, assignment-service and
 * notification-service, and enforces JWT-based perimeter authentication for
 * protected routes before forwarding downstream.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {

        System.out.println("CI/CD TEST - Version 2");
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
