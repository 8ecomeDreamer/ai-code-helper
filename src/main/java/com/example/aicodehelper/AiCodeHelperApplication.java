package com.example.aicodehelper;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.aicodehelper.mapper")
public class AiCodeHelperApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeHelperApplication.class, args);
    }

}
