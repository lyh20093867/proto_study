package com.moji.dao;

import com.moji.constant.SqlConstant;
import com.moji.pojo.AzkabanProject;
import com.moji.pojo.JobDataRel;
import com.moji.pool.AbstractDataSource;
import com.moji.pool.impl.TestDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;

@Slf4j
public class AzkabanDao {
    private AbstractDataSource dataSource;

    public AzkabanDao(AbstractDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HashMap<JobDataRel, HashSet<JobDataRel>>[] getDataJobRel(Integer projectId, Connection conn) throws SQLException {
        String sql = String.format(SqlConstant.PROJECT_JOB_REL, projectId);
        log.info("【project_job_rel_sql】" + sql);
        ResultSet res = dataSource.queryBySql(conn, sql);
        HashMap<JobDataRel, HashSet<JobDataRel>> sons = new HashMap<>();
        HashMap<JobDataRel, HashSet<JobDataRel>> parent = new HashMap<>();
        HashMap<JobDataRel, HashSet<JobDataRel>> jobParent = new HashMap<>();
        HashMap<JobDataRel, HashSet<JobDataRel>> jobSon = new HashMap<>();
        HashMap<JobDataRel, HashSet<JobDataRel>> dataParent = new HashMap<>();
        HashMap<JobDataRel, HashSet<JobDataRel>> dataSon = new HashMap<>();
        while (res.next()) {
            buildSonParent(res, sons, parent);
        }
        HashSet<JobDataRel> children = (HashSet) parent.keySet();
        children.addAll(sons.keySet());
        HashSet<String> used = new HashSet<>();
        while (children != null && children.size() > 0) {
            children = getData(conn, sons, parent, children, used);
        }
        buildSonParentWithSameType(sons, parent, jobParent, jobSon, dataParent, dataSon);
        log.info(String.format("son_size:%s,parent_size:%s,jobParent:%s,jobSon:%s,dataParent:%s,dataSon:%s",
                sons.size(), parent.size(), jobParent.size(), jobSon.size(), dataParent.size(), dataSon.size()));
        int[] a = new int[2];
        HashMap<JobDataRel, HashSet<JobDataRel>>[] arr = new HashMap[2];
        arr[0] = jobParent;
        arr[1] = jobSon;
        return arr;
    }

    private void buildSonParentWithSameType(HashMap<JobDataRel, HashSet<JobDataRel>> sons,
                                            HashMap<JobDataRel, HashSet<JobDataRel>> parent,
                                            HashMap<JobDataRel, HashSet<JobDataRel>> jobParent,
                                            HashMap<JobDataRel, HashSet<JobDataRel>> jobSon,
                                            HashMap<JobDataRel, HashSet<JobDataRel>> dataParent,
                                            HashMap<JobDataRel, HashSet<JobDataRel>> dataSon) {
        for (JobDataRel p : sons.keySet()) {
            getDataAndPut(null, p, sons, jobSon, dataSon, p.getJobType());
        }
        for (JobDataRel s : parent.keySet()) {
            getDataAndPut(null, s, parent, jobParent, dataParent, s.getJobType());
        }

    }

    private void getDataAndPut(JobDataRel another, JobDataRel job,
                               HashMap<JobDataRel, HashSet<JobDataRel>> sons,
                               HashMap<JobDataRel, HashSet<JobDataRel>> jobSon,
                               HashMap<JobDataRel, HashSet<JobDataRel>> dataSon,
                               String jobType) {
        if (sons.containsKey(job)) return;
        HashSet<JobDataRel> sameData = getFilterData(sons.get(job), jobType, "=");
        HashSet<JobDataRel> difData = getFilterData(sons.get(job), jobType, "!=");
        JobDataRel pre = another != null ? another : job;
        if (sameData.size() > 0) {
            if (jobType == "job") isContainAndPut(pre, jobSon, sameData);
            else isContainAndPut(pre, dataSon, sameData);
        }
        if (difData.size() > 0) for (JobDataRel j : difData) getDataAndPut(pre, j, sons, jobSon, dataSon, jobType);
    }

    private void isContainAndPut(JobDataRel job, HashMap<JobDataRel,
            HashSet<JobDataRel>> datas, HashSet<JobDataRel> set) {
        if (datas.containsKey(job)) {
            datas.get(job).addAll(set);
        } else {
            HashSet<JobDataRel> list = new HashSet<>();
            list.addAll(set);
            datas.put(job, list);
        }
    }

    private void checkAndPut(HashMap<JobDataRel, HashSet<JobDataRel>> dataSon, JobDataRel p, JobDataRel s) {
        if (dataSon.containsKey(p)) {
            dataSon.get(p).add(s);
        } else {
            HashSet<JobDataRel> list = new HashSet<>();
            list.add(s);
            dataSon.put(p, list);
        }
    }

    private HashSet<JobDataRel> getFilterData(HashSet<JobDataRel> jobData, String jobType, String s) {
        HashSet<JobDataRel> res = new HashSet<>();
        for (JobDataRel j : jobData) {
            if (s == "=") {
                if (jobType == j.getJobType()) res.add(j);
            } else {
                if (jobType != j.getJobType()) res.add(j);
            }

        }
        return res;
    }

    private HashSet<JobDataRel> getData(Connection conn, HashMap<JobDataRel, HashSet<JobDataRel>> sons,
                                        HashMap<JobDataRel, HashSet<JobDataRel>> parent,
                                        HashSet<JobDataRel> children, HashSet<String> used) throws SQLException {
        if (children != null && children.size() > 0) {
            HashSet<String> ids = getIds(children);
            HashSet<String> newIds = getNewIds(ids, used);
            if (newIds.size() > 0) {
                String newIdstr = getNewIdstr(newIds, ",");
                String fatherSql = String.format(SqlConstant.FATHER_DATA_JOB_REL, newIdstr);
                log.info("【fatherSql】" + fatherSql);
                ResultSet father = dataSource.queryBySql(conn, fatherSql);
                while (father.next()) {
                    buildSonParent(father, sons, parent);
                }
                String sonSql = String.format(SqlConstant.SON_DATA_JOB_REL, newIdstr);
                log.info("【sonSql】" + sonSql);
                ResultSet son = dataSource.queryBySql(conn, sonSql);
                while (son.next()) {
                    buildSonParent(son, sons, parent);
                }
                used.addAll(newIds);
                HashSet<JobDataRel> resData = (HashSet) parent.keySet();
                resData.addAll(sons.keySet());
                return resData;
            } else return null;
        } else return null;
    }

    private String getNewIdstr(HashSet<String> newIds, String s) {
        StringBuilder sb = new StringBuilder();
        for (String id : newIds) sb.append(id).append(s);
        return sb.toString().substring(0, sb.length() - 1);
    }

    private HashSet<String> getNewIds(HashSet<String> ids, HashSet<String> used) {
        HashSet<String> res = new HashSet<>();
        for (String id : ids) if (!used.contains(id)) res.add(id);
        return res;
    }

    private HashSet<String> getIds(HashSet<JobDataRel> children) {
        HashSet<String> res = new HashSet<>();
        for (JobDataRel j : children) res.add(j.getJobId());
        return res;
    }

    private void buildSonParent(ResultSet res, HashMap<JobDataRel, HashSet<JobDataRel>> sons,
                                HashMap<JobDataRel, HashSet<JobDataRel>> parent) throws SQLException {
        JobDataRel p = new JobDataRel();
        JobDataRel s = new JobDataRel();
        p.setJobId(res.getString(1));
        p.setJobType(res.getString(2));
        s.setJobId(res.getString(1));
        s.setJobType(res.getString(2));
        checkAndPut(sons, p, s);
        checkAndPut(parent, s, p);
    }

    public AzkabanProject getProjectById(Integer projectId, Connection conn) {
        String sql = String.format(SqlConstant.PROJECT_MYSQL, projectId, projectId, projectId);
        log.info("【project_sql】" + sql);
        AzkabanProject project = null;
        try {
            ResultSet res = dataSource.queryBySql(conn, sql);
            if (res.next()) {
                project = new AzkabanProject();
                project.setId(res.getInt(1));
                project.setProjectName(res.getString(2));
                project.setProjectExecFrequency(res.getString(3));
                project.setStep(res.getInt(4));
                project.setLastSuccExecId(res.getString(5));
                project.setLastSuccExecBeginDt(res.getString(6));
                project.setSelfActive(res.getString(7));
                project.setSelfBeginDt(res.getString(8));
                project.setSelfEndDt(res.getString(9));
                project.setUpdaterId(res.getInt(10));
                project.setUpdateTime(res.getString(11));
                project.setStatus(res.getInt(12));
                project.setProjectExecFrequency(res.getString(13));
                project.setProjectExecFrequency(res.getString(14));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return project;
    }
}
