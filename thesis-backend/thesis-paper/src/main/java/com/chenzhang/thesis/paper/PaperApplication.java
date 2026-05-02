package com.chenzhang.thesis.paper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.chenzhang.thesis.paper.feign")
@MapperScan("com.chenzhang.thesis.paper.mapper")
public class PaperApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaperApplication.class, args);
    }
}
