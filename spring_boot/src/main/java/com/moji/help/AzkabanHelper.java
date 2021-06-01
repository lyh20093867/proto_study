package com.moji.help;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.moji.constant.AzkabanConstant;
import com.moji.exception.JSONParseErrorException;
import com.moji.pojo.AzkabanJob;
import com.moji.pojo.AzkabanProject;
import com.moji.utils.LocalExectionUtils;
import com.moji.utils.ZipUtils;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import com.typesafe.config.ConfigFactory;

@Slf4j
public class AzkabanHelper {
    private LocalExectionUtils exector = new LocalExectionUtils();

    public String generateProjectZipFile(AzkabanProject project, AzkabanJob preJob) throws IOException {
        String sourceDirPath = project.getEntryHome() + "/temp";
        String finalJobDirPath = project.getEntryHome() + "/final";
        log.info("【sourceDirPath】" + sourceDirPath);
        log.info("【finalJobDirPath】" + finalJobDirPath);
        File sourceZipDir = new File(sourceDirPath);
        if (sourceZipDir.exists()) {
            String rmCommand = String.format("rm -r %s", sourceDirPath);
            log.info("【rmCommand】" + rmCommand);
            exector.exec(rmCommand);
        }
        sourceZipDir.mkdir();
        generateCommonParamFile(sourceZipDir, project);
        recursionPreJob(preJob, sourceZipDir, project);
        File finalJobDir = new File(finalJobDirPath);
        if (!finalJobDir.exists()) finalJobDir.mkdir();
        DateTimeFormatter currentMillsType = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String currentMillsDate = LocalDateTime.now(ZoneId.systemDefault()).format(currentMillsType);
        File zipFile = new File(finalJobDir + "/" + project.getProjectName() + "_" + currentMillsDate + ".zip");
        if (zipFile.exists()) zipFile.delete();
        ZipUtils zip = new ZipUtils();
        zip.zip(sourceZipDir, zipFile);
        return zipFile.getAbsolutePath();
    }

    private void recursionPreJob(AzkabanJob job, File sourceZipDir, AzkabanProject project) {
        job.setColor(10);
        generateJobFile(job, sourceZipDir, project);
        if (job.getNextNodes() != null)
            for (AzkabanJob j : job.getNextNodes()) if (j.getColor() != 10) recursionPreJob(j, sourceZipDir, project);
    }

    private void generateJobFile(AzkabanJob job, File sourceZipDir, AzkabanProject project) {
        String typeName = job.getJobTypeName().toLowerCase();
        String jobName = job.getJobName();
        String projectName = project.getProjectName();
        File jobFile = new File(sourceZipDir + "/" + jobName + ".job");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(jobFile));
            bw.write("type=command");
            bw.newLine();
            if (typeName == "pre") {
                String preJobCommand = String.format("command=sh +x %s/pre.sh --project_name '%s' --step '%s' --project_exec_frequency '%s' --self_active '%s' --last_succ_exec_begin_dt '%s' --self_start_dt '%s' --self_end_dt '%s'",
                        project.getAgentHome(),project.getProjectName(),project.getStep(),project.getProjectExecFrequency(), project.getSelfActive(), project.getLastSuccExecBeginDt(), project.getSelfBeginDt(), project.getSelfEndDt());
                log.info(String.format("【%s_preJobCommand】%s", projectName, preJobCommand));
                bw.write(preJobCommand);
            } else if (typeName == "post") {
                String postJobCommand = String.format("command=sh +x %s/post.sh --self_active '%s' --execid ${azkaban.flow.execid} --project_name '%s'", project.getAgentHome(), project.getSelfActive(),project.getProjectName());
                log.info(String.format("【%s_postJobCommand】%s", projectName, postJobCommand));
                bw.write(postJobCommand);
            } else if (typeName == "timer_trigger") {
                String timerTriggerCommand = String.format("command=%s/%s_task.sh '%s' ${azkaban.flow.projectid} ${azkaban.flow.flowid} ${azkaban.flow.execid}",
                        project.getAgentHome(), typeName, jobName);
                log.info("【timer_trigger_command】" + timerTriggerCommand);
                bw.write(timerTriggerCommand);
            } else {
                String context = job.getContext();
                String jobCommand = null;
                if (context != null && !context.trim().equals("")) {
                    jobCommand = String.format("command=sh +x %s/entry_sh.sh --script '%s' --server_location '%s' --path_location '%s' --agent_location %s/agent.sh --job_name '%s' --project_exec_frequency '%s' --server_name '%s' --start_dt '${start_dt}' --end_dt '${end_dt}' --projectid ${azkaban.flow.projectid} --flowid ${azkaban.flow.flowid} --execid ${azkaban.flow.execid}",
                            project.getAgentHome(), job.getContext(), job.getServerLocation(), job.getPathLocation(), project.getAgentHome(), jobName, project.getProjectExecFrequency(), job.getServerName());
                } else {
                    jobCommand = String.format("command=sh +x %s/entry_sh.sh --server_location '%s' --path_location '%s' %s/agent.sh --job_name '%s' --project_exec_frequency '%s' --server_name '%s' --start_dt '${start_dt}' --end_dt '${end_dt}' --projectid ${azkaban.flow.projectid} --flowid ${azkaban.flow.flowid} --execid ${azkaban.flow.execid}",
                            project.getAgentHome(), job.getServerLocation(), job.getPathLocation(), project.getAgentHome(), jobName, project.getProjectExecFrequency(), job.getServerName());
                }
                log.info(String.format("【%s_jobCommand】%s", jobName, jobCommand));
                bw.write(jobCommand);
            }
            bw.newLine();
            String dep = job.getDependencies();
            if (dep != null && dep != "") {
                String depStr = String.format("dependencies=%s", dep);
                log.info(String.format("【%s_dependencies】%s", jobName, depStr));
                bw.write(depStr);
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateCommonParamFile(File sourceZipDir, AzkabanProject project) {
        File file = new File(sourceZipDir, "common.properties");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write(String.format("project_name=%s", project.getProjectName()));
            bw.newLine();
            bw.write(String.format("entry_home=%s", project.getEntryHome()));
            bw.newLine();
            bw.write(String.format("agent_home=%s", project.getAgentHome()));
            bw.newLine();
            bw.write(String.format("last_succ_exec_id=%s", project.getLastSuccExecId()));
            bw.newLine();
            bw.write(String.format("last_succ_exec_begin_dt=%s", project.getLastSuccExecBeginDt()));
            bw.newLine();
            bw.write(String.format("project_exec_frequency=%s", project.getProjectExecFrequency()));
            bw.newLine();
            bw.write(String.format("self_begin_dt=%s", project.getSelfBeginDt()));
            bw.newLine();
            bw.write(String.format("self_end_dt=%s", project.getSelfEndDt()));
            bw.newLine();
            bw.write(String.format("self_active=%s", project.getSelfActive()));
            bw.newLine();
            bw.write(String.format("step=%s", project.getStep()));
            bw.newLine();
            bw.write(String.format("status=%s", project.getStatus()));
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String uploadProjectFile(AzkabanProject project, Set<AzkabanJob> keySet) throws IOException, JSONParseErrorException {
        AzkabanJob preJob = null;
        for (AzkabanJob j : keySet) preJob = j;
        log.info("【preJob】" + preJob);
        String projectZipFile = generateProjectZipFile(project, preJob);
        Config baseConf = ConfigFactory.load(AzkabanConstant.AKABAN_CONIF_FILE_NAME).getConfig(AzkabanConstant.ENV_AZKABAN);
        String sessionId = getAzkabanSessionId(baseConf);
        log.info("【sessionId】" + sessionId);
        /**
         * @TODO 此处在正式环境中需要移除，不应该有createProject
         */
        createProject(sessionId, project.getProjectName(), baseConf);
        return uploadProjectFile(sessionId, projectZipFile, project.getProjectName(), baseConf);
    }

    private String getAzkabanSessionId(Config baseConf) throws JSONParseErrorException {
        String loginCommand = baseConf.getString(AzkabanConstant.AZKABAN_AUTH_CMD)
                .replace("{USER}", baseConf.getString(AzkabanConstant.AZKABAN_USER))
                .replace("{PASSWORD}", baseConf.getString(AzkabanConstant.AZKABAN_PASSWORD))
                .replace("{IP}", baseConf.getString(AzkabanConstant.AZKABAN_IP))
                .replace("{PORT}", baseConf.getString(AzkabanConstant.AZKABAN_PORT));
        String execString = exector.exec(loginCommand);
        JSONObject resJosn = parseJsonString(execString);
        if (resJosn.containsKey("error")) {
            String error = resJosn.getString("error");
            log.error("【login_error】" + error);
            return error;
        } else {
            String sessionId = resJosn.getString("session.id");
            log.info("【sessionId】" + sessionId);
            return sessionId;
        }
    }

    private String uploadProjectFile(String sessionId, String projectZipFile, String projectName, Config baseConf) throws JSONParseErrorException {
        String uploadCommand = baseConf.getString(AzkabanConstant.AZKABAN_UPLOAD_CMD)
                .replace("{PROJECTNAME}", projectName)
                .replace("{SESSIONID}", sessionId)
                .replace("{PROJECTZIPFILE}", projectZipFile)
                .replace("{IP}", baseConf.getString(AzkabanConstant.AZKABAN_IP))
                .replace("{PORT}", baseConf.getString(AzkabanConstant.AZKABAN_PORT));
        String execString = exector.exec(uploadCommand);
        JSONObject resJosn = parseJsonString(execString);
        String result = "";
        if (resJosn.containsKey("error"))
            result = String.format("【upload_error】error_message:%s", resJosn.getString("error"));
        else
            result = String.format("【upload_success】projectId:%s,version:%s", resJosn.getString("projectId"), resJosn.getString("version"));
        return result;
    }

    private JSONObject parseJsonString(String execString) throws JSONParseErrorException {
        JSONObject res = null;
        String logs = execString.substring(execString.indexOf("{"));
        log.info("【exec_result】" + logs);
        try {
            res = JSON.parseObject(logs);
        } catch (Exception e) {
            JSONParseErrorException ex = new JSONParseErrorException(String.format("【JSON_ERROR】message:%s,log:%s", e.getMessage(), logs));
            throw ex;
        }
        return res;
    }

    private String createProject(String sessionId, String projectName, Config baseConf) throws JSONParseErrorException {
        String ip = baseConf.getString(AzkabanConstant.AZKABAN_IP);
        String port = baseConf.getString(AzkabanConstant.AZKABAN_PORT);
        String createCommand = baseConf.getString(AzkabanConstant.AZKABAN_CREATE_PROJECT_CMD)
                .replace("{PROJECTNAME}", projectName)
                .replace("{DESCRIPTION}", projectName)
                .replace("{SESSIONID}", sessionId)
                .replace("{IP}", ip)
                .replace("{PORT}", port);
        String execString = exector.exec(createCommand);
        System.out.println(execString);
        JSONObject resJosn = parseJsonString(execString);
        String status = resJosn.getString("status");
        log.info("【status】" + status);
        if (status.trim().equals("error")) {
            log.error("【create_error】" + resJosn.getString("message"));
        }
        return status;

    }

}
