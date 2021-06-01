package com.moji.controller;


import com.moji.dao.AzkabanDao;
import com.moji.exception.EmptyJobException;
import com.moji.exception.JSONParseErrorException;
import com.moji.exception.RingLoopException;
import com.moji.help.AzkabanHelper;
import com.moji.pojo.AzkabanJob;
import com.moji.pojo.AzkabanProject;
import com.moji.pojo.JobDataRel;
import com.moji.pool.impl.TestDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;


@RestController
@RequestMapping(value = "/azkaban")
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
    @RequestMapping("/proj/upload")
    public String upload(@RequestParam(value = "projectId") Integer projectId) {
        String res;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            AzkabanDao dao = new AzkabanDao(dataSource);
            AzkabanProject project = dao.getProjectById(projectId, conn);
            log.info("【project】" + project);
            HashMap<JobDataRel, HashSet<JobDataRel>>[] dataJobRel = dao.getDataJobRelNew(projectId, conn);
            HashMap<JobDataRel, HashSet<JobDataRel>> jobParent = dataJobRel[0];
            HashMap<JobDataRel, HashSet<JobDataRel>> jobSon = dataJobRel[1];
            HashMap<AzkabanJob, HashSet<AzkabanJob>>[] azkabanData = dao.getAzkabanJobList(jobSon, jobParent, conn);
            HashMap<AzkabanJob, HashSet<AzkabanJob>> preJob = azkabanData[2];
            AzkabanHelper helper = new AzkabanHelper();
            String message = helper.uploadProjectFile(project, preJob.keySet());
            log.info(message);
            res = String.format("{\"status\":\"success\",\"code\":200,\"message\":\"%s\"}",message);
        } catch (JSONParseErrorException e) {
            res = String.format("{\"code\":601,\"status\":\"error\",\"message\":\"%s\"}",e.getMessage());
            e.printStackTrace();
        } catch (EmptyJobException e) {
            res = String.format("{\"code\":602,\"status\":\"error\",\"message\":\"%s\"}",e.getMessage());
            e.printStackTrace();
        } catch (RingLoopException ex) {
            res = String.format("{\"code\":603,\"status\":\"error\",\"message\":\"%s\"}",ex.getMessage());
            ex.printStackTrace();
        } catch (SQLException e) {
            res = String.format("{\"code\":604,\"status\":\"error\",\"message\":\"%s\"}",e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            res = String.format("{\"code\":605,\"status\":\"error\",\"message\":\"%s\"}",e.getMessage());
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    res = String.format("{\"code\":604,\"status\":\"error\",\"message\":\"%s\"}",e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return res;
    }

}
