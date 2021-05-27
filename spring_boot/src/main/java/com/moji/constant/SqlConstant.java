package com.moji.constant;

public class SqlConstant {
    public static String PROJECT_MYSQL = "select a.id as id,name as project_name,b.value_en as project_exec_frequency,step,last_succ_exec_id,last_succ_exec_begin_dt,self_active," +
            "self_begin_dt,self_end_dt,updater_id,update_time,a.status as status,c.value_en as agent_home,d.value_en as entry_home from" +
            "(select * from moji_project where id = %s) as a inner join" +
            "(select id,value_en,value_cn,status from moji_constant) as b on a.project_exec_frequency_cid = b.id " +
            "inner join ( SELECT %s as id, value_en FROM moji_constant WHERE NAME = 'agent_home' ) AS c ON a.id = c.id " +
            "INNER JOIN ( SELECT %s as id, value_en FROM moji_constant WHERE NAME = 'entry_home' ) AS d ON a.id = d.id";
    public static String PROJECT_JOB_REL = "select b.father_id,b.father_type,b.son_id,b.son_type " +
            "from (SELECT job_id FROM moji_project_job_rel WHERE project_id = %s) as a " +
            "inner join (select father_type, father_id, son_id, son_type from moji_job_data_rel) as b on a.job_id = b.father_id";
    public static String FATHER_DATA_JOB_REL = "select father_id,father_type, son_id, son_type from moji_job_data_rel where father_id in (%s)";
    public static String SON_DATA_JOB_REL = "select father_id,father_type, son_id, son_type from moji_job_data_rel where son_id in (%s)";
    public static String AZKABAN_JOB = "select a.id as id,a.name as job_name,a.job_type_cid as job_type_id,d.value_en as job_type_name," +
            "a.server_locationid as server_id, b.value as server_location,a.path_locationid as path_id,c.value as path_location," +
            "b.name as server_name from ( SELECT id, NAME, name_cn, job_type_cid, server_locationid, path_locationid, pri, descr FROM moji_job where id in (%s)) AS a " +
            "left JOIN ( SELECT id, NAME, VALUE, pid, descr FROM moji_joblocation ) AS b ON a.server_locationid = b.id " +
            "left JOIN ( SELECT id, NAME, VALUE, pid, descr FROM moji_joblocation ) AS c ON a.path_locationid = c.id " +
            "left join (select id,value_en,value_cn,status from moji_constant) as d on a.job_type_cid = d.id";
}
