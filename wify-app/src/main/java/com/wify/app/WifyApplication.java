package com.wify.app;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.wify")
@MapperScan("com.wify.**.mapper")
public class WifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(WifyApplication.class, args);
    }
}
