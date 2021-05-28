package com.moji.controller;


import com.moji.constant.SqlConstant;
import com.moji.dao.AzkabanDao;
import com.moji.exception.RingLoopException;
import com.moji.help.AzkabanHelper;
import com.moji.pojo.AzkabanJob;
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

import java.io.IOException;
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
    public String upload(@RequestParam(value = "projectId", defaultValue = "47") Integer projectId) {
        String res;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            AzkabanDao dao = new AzkabanDao(dataSource);
            AzkabanProject project = dao.getProjectById(projectId, conn);
            log.debug("【project】" + project);
            HashMap<JobDataRel, HashSet<JobDataRel>>[] dataJobRel = dao.getDataJobRel(projectId, conn);
            HashMap<JobDataRel, HashSet<JobDataRel>> jobParent = dataJobRel[0];
            HashMap<JobDataRel, HashSet<JobDataRel>> jobSon = dataJobRel[1];
            HashMap<AzkabanJob, HashSet<AzkabanJob>>[] azkabanData = dao.getAzkabanJobList(jobSon, jobParent, conn);
//            HashMap<AzkabanJob, HashSet<AzkabanJob>> azkabanJobSons = azkabanData[0];
//            HashMap<AzkabanJob, HashSet<AzkabanJob>> azkabanJobParent = azkabanData[1];
            HashMap<AzkabanJob, HashSet<AzkabanJob>> preJob = azkabanData[2];
            AzkabanHelper helper = new AzkabanHelper();
            String message = helper.uploadProjectFile(project, preJob.keySet());
            log.debug(message);
            res = message;
        } catch (RingLoopException ex) {
            res = ex.getMessage();
            ex.printStackTrace();
        } catch (SQLException throwables) {
            res = throwables.getMessage();
            throwables.printStackTrace();
        } catch (IOException e) {
            res = e.getMessage();
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
        return res;
    }

}
