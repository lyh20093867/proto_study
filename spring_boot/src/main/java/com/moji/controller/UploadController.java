package com.moji.controller;


import com.moji.constant.SqlConstant;
import com.moji.dao.AzkabanDao;
import com.moji.pojo.AzkabanProject;
import com.moji.pojo.JobDataRel;
import com.moji.pool.impl.TestDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/upload")
@Slf4j
public class UploadController {

    @Autowired
    TestDataSource dataSource;

    @Bean
    public TestDataSource getTestDataSource() {
        return new TestDataSource();
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }

    /**
     * 根据传递的projectId构建DAG
     *
     * @param projectId
     * @return
     * @throws Exception
     */
    @RequestMapping("/azkb/proj/upload")
    public String upload(@RequestParam(value = "projectId", defaultValue = "47") Integer projectId) throws Exception {
        Connection conn = dataSource.getConnection();
        AzkabanDao dao = new AzkabanDao(dataSource);
        AzkabanProject project = dao.getProjectById(projectId, conn);
        dao.getDataJobRel(projectId, conn);
        System.out.println(projectId);
        System.out.println(project);
        return "";
    }

}
