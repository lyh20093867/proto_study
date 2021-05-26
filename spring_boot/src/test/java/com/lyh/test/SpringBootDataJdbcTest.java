//package com.lyh.test;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//import java.sql.Connection;
//import java.sql.SQLException;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//@SpringBootApplication
//public class SpringBootDataJdbcTest {
//
//    @Autowired
//    private DataSource dataSource;
//    @Autowired
////    private ProviderController controller;
//    @Autowired
//    private TestRestTemplate restTemplate;
//    @Test
//    public void contextLoad() throws SQLException {
//        System.out.println("datasource:" + dataSource.getClass());
//        Connection conn = dataSource.getConnection();
//        System.out.println(conn);
//        conn.close();
//        assertThat(controller).isNotNull();
//        assertThat(this.restTemplate.getForObject("http://localhost:8080/",String.class)).contains("Hello");
//    }
//}
