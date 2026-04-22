package com.chenzhang.thesis.defense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DefenseApplication {
    public static void main(String[] args) {
        SpringApplication.run(DefenseApplication.class, args);
    }
}
