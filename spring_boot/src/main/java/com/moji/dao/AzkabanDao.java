package com.moji.dao;

import com.moji.constant.SqlConstant;
import com.moji.exception.RingLoopException;
import com.moji.pojo.AzkabanEdge;
import com.moji.pojo.AzkabanJob;
import com.moji.pojo.AzkabanProject;
import com.moji.pojo.JobDataRel;
import com.moji.pool.AbstractDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

@Slf4j
public class AzkabanDao {
    private AbstractDataSource dataSource;

    public AzkabanDao(AbstractDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public HashMap<JobDataRel, HashSet<JobDataRel>>[] getDataJobRel(Integer projectId, Connection conn) throws SQLException {
        String sql = String.format(SqlConstant.PROJECT_JOB_REL, projectId);
        log.debug("【project_job_rel_sql】" + sql);
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
        HashSet<JobDataRel> children = new HashSet<>(parent.keySet());
        if (sons != null && sons.keySet() != null && sons.keySet().size() > 0) {
            children.addAll(sons.keySet());
        }
        HashSet<String> used = new HashSet<>();
        while (children != null && children.size() > 0) {
            children = getData(conn, sons, parent, children, used);
        }
        buildSonParentWithSameType(sons, parent, jobParent, jobSon, dataParent, dataSon);
        log.debug(String.format("son_size:%s,parent_size:%s,jobParent:%s,jobSon:%s,dataParent:%s,dataSon:%s",
                sons.size(), parent.size(), jobParent.size(), jobSon.size(), dataParent.size(), dataSon.size()));
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
        if (!sons.containsKey(job)) return;
        HashSet<JobDataRel> sameData = getFilterData(sons.get(job), jobType, "=");
        HashSet<JobDataRel> difData = getFilterData(sons.get(job), jobType, "!=");
        JobDataRel pre = another != null ? another : job;
        if (sameData.size() > 0) {
            if ("job".equals(jobType.trim().toLowerCase())) isContainAndPut(pre, jobSon, sameData);
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
                if (jobType.trim().equals(j.getJobType().trim())) res.add(j);
            } else {
                if (!jobType.trim().equals(j.getJobType().trim())) res.add(j);
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
                if (newIdstr.endsWith(",")) {
                    newIdstr = newIdstr.substring(0, newIdstr.length() - 1);
                }
                log.debug("【newIdstr】" + newIdstr);
                String fatherSql = String.format(SqlConstant.FATHER_DATA_JOB_REL, newIdstr);
                log.debug("【fatherSql】" + fatherSql);
                ResultSet father = dataSource.queryBySql(conn, fatherSql);
                while (father.next()) {
                    buildSonParent(father, sons, parent);
                }
                String sonSql = String.format(SqlConstant.SON_DATA_JOB_REL, newIdstr);
                log.debug("【sonSql】" + sonSql);
                ResultSet son = dataSource.queryBySql(conn, sonSql);
                while (son.next()) {
                    buildSonParent(son, sons, parent);
                }
                used.addAll(newIds);
                HashSet<JobDataRel> resData = new HashSet<>(parent.keySet());
                resData.addAll(sons.keySet());
                return resData;
            } else return null;
        } else return null;
    }

    private String getNewIdstr(HashSet<String> newIds, String s) {
        StringBuilder sb = new StringBuilder();
        for (String id : newIds) sb.append(id).append(s);
        return sb.toString().substring(0, sb.toString().length() - 1);
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
        s.setJobId(res.getString(3));
        s.setJobType(res.getString(4));
        checkAndPut(sons, p, s);
        checkAndPut(parent, s, p);
    }

    public AzkabanProject getProjectById(Integer projectId, Connection conn) {
        String sql = String.format(SqlConstant.PROJECT_MYSQL, projectId, projectId, projectId);
        log.debug("【project_sql】" + sql);
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
                project.setAgentHome(res.getString(13));
                project.setEntryHome(res.getString(14));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return project;
    }

    public HashMap<AzkabanJob, HashSet<AzkabanJob>>[] getAzkabanJobList(HashMap<JobDataRel, HashSet<JobDataRel>> jobSon,
                                                                        HashMap<JobDataRel, HashSet<JobDataRel>> jobParent,
                                                                        Connection conn) throws SQLException, RingLoopException {
        HashSet<JobDataRel> jobTotalSet = new HashSet<>();
        jobTotalSet.addAll(jobSon.keySet());
        jobTotalSet.addAll(jobParent.keySet());
        for (HashSet<JobDataRel> set : jobSon.values()) jobTotalSet.addAll(set);
        for (HashSet<JobDataRel> set : jobParent.values()) jobTotalSet.addAll(set);
        HashMap<String, AzkabanJob> jobDataRelJobs = getAzkabanJobFromMysql(conn, jobTotalSet);
        log.debug("【jobDataRelJobs.size】" + jobDataRelJobs.size());
        HashMap<AzkabanJob, HashSet<AzkabanJob>> azkabanJobSons = new HashMap<>();
        HashMap<AzkabanJob, HashSet<AzkabanJob>> azkabanJobParent = new HashMap<>();
        for (JobDataRel p : jobSon.keySet()) {
            HashSet<JobDataRel> sons = jobSon.get(p);
            HashSet<AzkabanJob> sonJobs = getAzkabanJobSet(sons, jobDataRelJobs);
            AzkabanJob parentJob = jobDataRelJobs.get(p.getJobId());
            HashSet<AzkabanEdge> nextEdges = getAzkabanEdges(sonJobs, parentJob);
            parentJob.setNextNodes(sonJobs);
            parentJob.setNextEdges(nextEdges);
            parentJob.setOut(sonJobs.size());
            azkabanJobSons.put(parentJob, sonJobs);
        }
        for (JobDataRel s : jobParent.keySet()) {
            HashSet<JobDataRel> parents = jobParent.get(s);
            HashSet<AzkabanJob> parentJobs = getAzkabanJobSet(parents, jobDataRelJobs);
            AzkabanJob sonJob = jobDataRelJobs.get(s.getJobId());
            sonJob.setIn(parentJobs.size());
            if (sonJob.getNextNodes() == null) sonJob.setNextNodes(new HashSet<>());
            String dependcies = getDependcies(parentJobs);
            log.debug(String.format("【%s_dependcies】%s", sonJob.getJobName(), dependcies));
            sonJob.setDependencies(dependcies);
            azkabanJobParent.put(sonJob, parentJobs);
        }
        HashSet<AzkabanJob> leafJob = getSetByTwoSet(azkabanJobParent, azkabanJobSons, "f");
        HashSet<AzkabanJob> oldJob = getSetByTwoSet(azkabanJobParent, azkabanJobSons, "o");
        AzkabanJob preJob = new AzkabanJob();
        preJob.setId("-1");
        preJob.setJobName("flow_pre_job");
        preJob.setJobTypeName("pre");
        preJob.setJobTypeId("-1");
        preJob.setOut(oldJob.size());
        preJob.setIn(0);
        preJob.setNextNodes(oldJob);
        HashSet<AzkabanEdge> preNextEdges = getAzkabanEdges(oldJob, preJob);
        preJob.setNextEdges(preNextEdges);
        AzkabanJob postJob = new AzkabanJob();
        postJob.setId("-1");
        postJob.setJobName("flow_post_job");
        postJob.setJobTypeName("post");
        postJob.setJobTypeId("-1");
        postJob.setIn(leafJob.size());
        postJob.setOut(0);
        HashSet<AzkabanJob> postSet = new HashSet<>();
        postSet.add(postJob);
        HashSet<AzkabanJob> preSet = new HashSet<>();
        preSet.add(preJob);
        for (AzkabanJob j : leafJob) azkabanJobSons.put(j, postSet);
        for (AzkabanJob j : oldJob) azkabanJobParent.put(j, preSet);
        log.debug("【azkabanJobSons.size】" + azkabanJobSons.size());
        log.debug("【azkabanJobParent.size】" + azkabanJobParent.size());
        checkRingLoop(preJob);
        HashMap<AzkabanJob, HashSet<AzkabanJob>>[] returnData = new HashMap[3];
        returnData[0] = azkabanJobSons;
        returnData[1] = azkabanJobParent;
        HashMap<AzkabanJob, HashSet<AzkabanJob>> preJobMap = new HashMap<>();
        HashSet<AzkabanJob> preJobSet = new HashSet<>();
        preJobSet.add(preJob);
        preJobMap.put(preJob, preJobSet);
        returnData[2] = preJobMap;
        return returnData;
    }

    private String getDependcies(HashSet<AzkabanJob> parentJobs) {
        StringBuilder sb = new StringBuilder();
        for (AzkabanJob j : parentJobs) sb.append(j.getJobName()).append(",");
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    private void checkRingLoop(AzkabanJob preJob) throws RingLoopException {
        Stack<AzkabanJob> stack = new Stack<>();
        Stack<Boolean> ch = new Stack<>();
        ch.push(false);
        hasRingLoopNode(preJob, stack, ch);
        boolean hasLoop = ch.peek();
        log.debug("【hasLoop】" + hasLoop);
        if (hasLoop) {
            log.error("任务存在环,请重新配置：");
            StringBuilder sb = new StringBuilder();
            printNode(sb, stack);
            log.error(sb.toString());
            RingLoopException ex = new RingLoopException("【ringLoopCheckError】存在环：" + sb.toString());
            throw ex;
        }
    }

    private void printNode(StringBuilder sb, Stack<AzkabanJob> job) {
        if (job != null && job.getNextNodes() != null && job.getNextNodes().size() != 0) {
            sb.append(job.getJobName()).append("->");
            if (job.getColor() == 4) job.setColor(5);
            else job.setColor(4);
            for (AzkabanJob j : job.getNextNodes()) {
                if (j.getColor() != 5) printNode(sb, j);
            }
        }
    }

    private void hasRingLoopNode(AzkabanJob firstNode, Stack<AzkabanJob> stack, Stack<Boolean> ch) {
        if (firstNode == null) return;
        firstNode.setColor(1);
        stack.push(firstNode);
        for (AzkabanJob n : firstNode.getNextNodes()) {
            if (stack.contains(n)) {
                Boolean res = (ch.pop() || true);
                ch.push(res);
                stack.push(n);
            }
            if (n.getColor() != 1) {
                hasRingLoopNode(n, stack, ch);
            }
        }
        if (!ch.peek()) stack.pop();
    }

    private boolean hasRingLoopNode(AzkabanJob next, AzkabanJob pre, AzkabanJob loopNode) {
        boolean res = false;
        if (next == null) return false;
        next.setColor(1);
        if (next.getNextNodes() == null || next.getNextNodes().size() == 0) return false;
        for (AzkabanJob j : next.getNextNodes()) {
            if (j != null && j.getColor() != 1) res = (res || hasRingLoopNode(j, next, loopNode));
            else if (j != null && j != pre && j.getNextNodes() != null && j.getNextNodes().size() != 0) {
                res = true;
                loopNode = j;
            }
        }
        return res;
    }

    private HashSet<AzkabanJob> getSetByTwoSet(HashMap<AzkabanJob, HashSet<AzkabanJob>> azkabanJobParent,
                                               HashMap<AzkabanJob, HashSet<AzkabanJob>> azkabanJobSons,
                                               String f) {
        HashSet<AzkabanJob> res = new HashSet<>();
        if ("f".equals(f)) {
            for (AzkabanJob j : azkabanJobParent.keySet()) if (!azkabanJobSons.containsKey(j)) res.add(j);
        } else {
            for (AzkabanJob j : azkabanJobSons.keySet()) if (!azkabanJobParent.containsKey(j)) res.add(j);
        }
        return res;
    }

    private HashSet<AzkabanEdge> getAzkabanEdges(HashSet<AzkabanJob> sonJobs, AzkabanJob p) {
        HashSet<AzkabanEdge> res = new HashSet<>();
        for (AzkabanJob j : sonJobs) {
            AzkabanEdge e = new AzkabanEdge();
            e.setFromJobId(p.getId());
            e.setFromJobName(p.getJobName());
            e.setToJobId(j.getId());
            e.setToJobName(j.getJobName());
            res.add(e);
        }
        return res;
    }

    private HashSet<AzkabanJob> getAzkabanJobSet(HashSet<JobDataRel> sons, HashMap<String, AzkabanJob> azkaBanJobDataRelDatas) {
        HashSet<AzkabanJob> res = new HashSet<>();
        for (JobDataRel j : sons) res.add(azkaBanJobDataRelDatas.get(j.getJobId()));
        return res;
    }

    private HashMap<String, AzkabanJob> getAzkabanJobFromMysql(Connection conn,
                                                               HashSet<JobDataRel> jobTotalSet) throws SQLException {
        HashSet<String> ids = getIds(jobTotalSet);
        String idStr = getNewIdstr(ids, ",");
        log.debug("【idStr】" + idStr);
        String sql = String.format(SqlConstant.AZKABAN_JOB, idStr);
        log.debug("【azkabanSql】" + sql);
        ResultSet res = dataSource.queryBySql(conn, sql);
        HashMap<String, AzkabanJob> azkabanJobMap = new HashMap<String, AzkabanJob>();
        while (res.next()) {
            String jobId = res.getString(1);
            AzkabanJob job = new AzkabanJob();
            job.setId(jobId);
            job.setJobName(res.getString(2));
            job.setJobTypeId(res.getString(3));
            job.setJobTypeName(res.getString(4));
            job.setServerId(res.getString(5));
            job.setServerLocation(res.getString(6));
            job.setPathId(res.getString(7));
            job.setPathLocation(res.getString(8));
            job.setServerName(res.getString(9));
            azkabanJobMap.put(jobId, job);
        }
        return azkabanJobMap;
    }
}
