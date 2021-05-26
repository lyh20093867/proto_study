//package com.lyh.test;
//
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicLong;
//
//@RestController
//public class ProviderController {
//
//    private static final String template = "Hello,%s !";
//    private final AtomicLong counter = new AtomicLong();
//
//    @GetMapping("/greeting")
//    public Greeting greeting(@RequestParam(value = "name", defaultValue = "Java") String name) {
//        return new Greeting(counter.incrementAndGet(), String.format(template, name));
//    }
//
//    @RequestMapping(path = "/greeting2", method = RequestMethod.GET)
//    public Greeting greeting2(@RequestParam(value = "name", defaultValue = "Java") String name) {
//        return new Greeting(counter.incrementAndGet(), String.format(template, name));
//    }
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @RequestMapping("/searchAll")
//    public List<AzkabanProject> list() {
//        String sql = "select * from moji_project";
//        return jdbcTemplate.query(sql, new BeanPropertyRowMapper(AzkabanProject.class));
//    }
//
//    @RequestMapping("/searchId")
//    public AzkabanProject queryById() {
//        String sql = "select * from moji_project where id = 47";
//        AzkabanProject res = jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(AzkabanProject.class));
//        System.out.println(res.toString());
//        return res;
//    }
//
//    @RequestMapping(path = "/jobDataRel", method = RequestMethod.GET)
//    public JobDataRel queryByFatherId(@RequestParam(name = "fatherId", defaultValue = "47") Integer fatherId) {
//        String sql = "select * from moji_job_data_rel where father_id = %s";
//        JobDataRel res = jdbcTemplate.queryForObject(String.format(sql, fatherId), new BeanPropertyRowMapper<>(JobDataRel.class));
//        System.out.println(res.toString());
//        return res;
//    }
//
//    @RequestMapping("/search")
//    public Map<String, Object> query() {
//        String sql = "select * from moji_project where id = 47";
//        return jdbcTemplate.queryForList(sql).get(0);
//    }
//
//    @RequestMapping("/proj")
//    public String getDAGByProjectId(@RequestParam(value = "projectId", defaultValue = "47") Integer projectId) throws Exception {
//        String sql = "select * from moji_project_job_rel where project_id = %s";
//        String fatherSql = "select * from moji_job_data_rel where father_id = %s";
//        List<ProjectJobRel> jobs = jdbcTemplate.queryForList(String.format(sql, projectId), ProjectJobRel.class);
//        // type_id
//        HashMap<String, String> parents = new HashMap<>();
//        HashMap<String, String> sons = new HashMap<>();
//        for (ProjectJobRel j : jobs) {
//            Integer jobId = j.getJobId();
//            getDataFromMysql(parents, sons, fatherSql, jobId + "");
//        }
//        getSonId(parents, sons);
//
//        return "";
//    }
//
//    private void getSonId(HashMap<String, String> parents, HashMap<String, String> sons) {
//        String fatherSql = "select * from moji_job_data_rel where father_id = %s";
//        for (String jobId : sons.keySet()) {
//            if (!parents.keySet().contains(jobId)) {
//                String id = jobId.split("_")[1];
//                getDataFromMysql(parents, sons, fatherSql, id);
//            }
//        }
//    }
//
//    private void getDataFromMysql(HashMap<String, String> parents, HashMap<String, String> sons, String fatherSql, String id) {
//        JobDataRel res = jdbcTemplate.queryForObject(String.format(fatherSql, id), new BeanPropertyRowMapper<>(JobDataRel.class));
//        if (res != null) {
//            String fatherType = res.getFatherType();
//            String sonType = res.getSonType();
//            int sonId = res.getSonId();
//            sons.put(fatherType + "_" + id, sonType + "_" + sonId);
//            parents.put(sonType + "_" + sonId, fatherType + "_" + id);
//        }
//    }
//
//    /**
//     * 根据传递的projectId构建DAG
//     *
//     * @param projectId
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping("/azkb/proj/upload")
//    public String upload(@RequestParam(value = "projectId", defaultValue = "47") Integer projectId) throws Exception {
//        // 构建DAG图：1、根据projectId从moji_project中查工程数据；2、根据projectId从moji_project_job_rel中获取一个projectid中的所有任务的job_id；
//        // 3、根据job_id去moji_job_data_rel中获取一个任务的所有父子任务以及它的依赖关系
//        String sql = "select * from moji_project_job_rel where project_id = %s";
//        String fatherSql = "select * from moji_job_data_rel where father_id = %s";
//        List<ProjectJobRel> jobs = jdbcTemplate.queryForList(String.format(sql, projectId), ProjectJobRel.class);
//        // type_id
//        HashMap<String, String> parents = new HashMap<>();
//        HashMap<String, String> sons = new HashMap<>();
//        for (ProjectJobRel j : jobs) {
//            Integer jobId = j.getJobId();
//            getDataFromMysql(parents, sons, fatherSql, jobId + "");
//        }
//        getSonId(parents, sons);
//
//        return "";
//    }
//}
