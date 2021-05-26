//package com.lyh.test;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.*;
//import org.springframework.boot.autoconfigure.*;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.web.bind.annotation.*;
//
//import javax.sql.DataSource;
//import java.util.List;
//import java.util.Map;
//
///**
// * @description: 运行后，在浏览器上输入http://localhost:8080/ 就可以访问了，返回Hello World!
// * 在后台$path/proto_study/spring_boot目录下运行 ：mvn spring-boot:run 就启动了程序
// * 在后台$path/proto_study/spring_boot目录下运行 ：mvn package 打包
// * @author: lyh
// * @date: 2021/5/11
// * @version: v1.0
// */
//@RestController
////@EnableAutoConfiguration
//@SpringBootApplication
//public class Example {
//
//    @RequestMapping("/")
//    String home() {
//        return "Hello My First Spring Boot!";
//    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(Example.class, args);
//    }
//}
