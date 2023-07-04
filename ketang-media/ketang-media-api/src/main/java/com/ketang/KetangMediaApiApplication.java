package com.ketang;

import com.spring4all.swagger.EnableSwagger2Doc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSwagger2Doc
public class KetangMediaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KetangMediaApiApplication.class, args);
    }

}
