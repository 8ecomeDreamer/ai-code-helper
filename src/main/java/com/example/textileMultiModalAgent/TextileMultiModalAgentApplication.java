package com.example.textileMultiModalAgent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.textileMultiModalAgent.mapper")
public class TextileMultiModalAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextileMultiModalAgentApplication.class, args);
    }

}
