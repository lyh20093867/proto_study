package com.moji;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
// @SpringBootApplication(scanBasePackages = "com.moji", exclude = {DataSourceAutoConfiguration.class})
@EnableCaching
@SpringBootApplication(scanBasePackages = "com.moji")
public class UploadProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(UploadProjectApplication.class, args);
    }
}
