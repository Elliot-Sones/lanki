package com.lanki;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LankiApplication {

    public static void main(String[] args) {
        SpringApplication.run(LankiApplication.class, args);
    }
}
