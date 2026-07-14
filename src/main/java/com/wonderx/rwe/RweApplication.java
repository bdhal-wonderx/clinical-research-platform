package com.wonderx.rwe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RweApplication {

    public static void main(String[] args) {
        SpringApplication.run(RweApplication.class, args);
    }
}
