package com.moji.help;

import ch.qos.logback.core.util.LocationUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.moji.constant.AzkabanConstant;
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
        log.debug("【sourceDirPath】" + sourceDirPath);
        log.debug("【finalJobDirPath】" + finalJobDirPath);
        File sourceZipDir = new File(sourceDirPath);
        if (sourceZipDir.exists()) {
            String rmCommand = String.format("rm -r %s", sourceDirPath);
            log.debug("【rmCommand】" + rmCommand);
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
        if (typeName != "pre" && typeName != "post") {
            String jobName = job.getJobName();
            File jobFile = new File(sourceZipDir + "/" + jobName + ".job");
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(new FileWriter(jobFile));
                bw.write("type=command");

                bw.newLine();
                if (typeName == "timer_trigger") {
                    String timer_trigger_command = String.format("comman=%s/%s_task.sh %s ${azkaban.flow.projectid} ${azkaban.flow.flowid} ${azkaban.flow.execid}",
                            project.getEntryHome(), typeName, jobName);
                    log.debug("【timer_trigger_command】" + timer_trigger_command);
                    bw.write(timer_trigger_command);
                } else {
                    String jobCommand = String.format("command=sh +x %s/entry_sh.sh %s %s %s/agent.sh %s %s %s ${azkaban.flow.projectid} ${azkaban.flow.flowid} ${azkaban.flow.execid}",
                            project.getEntryHome(), job.getServerLocation(), project.getAgentHome(), job.getPathLocation(), jobName, project.getProjectExecFrequency(), job.getServerName());
                    log.debug(String.format("【%s_jobCommand】%s", jobName, jobCommand));
                    bw.write(jobCommand);
                }
                bw.newLine();
                String dep = job.getDependencies();
                if (dep != null && dep != "") {
                    String depStr = String.format("dependencies=%s", dep);
                    log.debug(String.format("【%s_dependencies】%s", jobName, depStr));
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

    public String uploadProjectFile(AzkabanProject project, Set<AzkabanJob> keySet) throws IOException {
        AzkabanJob preJob = null;
        for (AzkabanJob j : keySet) preJob = j;
        log.debug("【preJob】" + preJob);
        String projectZipFile = generateProjectZipFile(project, preJob);
        Config baseConf = ConfigFactory.load(AzkabanConstant.AKABAN_CONIF_FILE_NAME).getConfig(AzkabanConstant.ENV_AZKABAN);
        String sessionId = getAzkabanSessionId(baseConf);
        log.debug("【sessionId】" + sessionId);
        createProject(sessionId, project.getProjectName(), baseConf);
        return uploadProjectFile(sessionId, projectZipFile, project.getProjectName(), baseConf);
    }

    private String getAzkabanSessionId(Config baseConf) {
        String loginCommand = baseConf.getString(AzkabanConstant.AZKABAN_AUTH_CMD)
                .replace("{USER}", baseConf.getString(AzkabanConstant.AZKABAN_USER))
                .replace("{PASSWORD}", baseConf.getString(AzkabanConstant.AZKABAN_PASSWORD))
                .replace("{IP}", baseConf.getString(AzkabanConstant.AZKABAN_IP))
                .replace("{PORT}", baseConf.getString(AzkabanConstant.AZKABAN_PORT));
        String execString = exector.exec(loginCommand);
        JSONObject resJosn = JSON.parseObject(execString.substring(execString.indexOf("{")));
        if (resJosn.containsKey("error")) {
            String error = resJosn.getString("error");
            log.error("【login_error】" + error);
            return error;
        } else {
            String sessionId = resJosn.getString("session.id");
            log.debug("【sessionId】" + sessionId);
            return sessionId;
        }
    }

    private String uploadProjectFile(String sessionId, String projectZipFile, String projectName, Config baseConf) {
        String uploadCommand = baseConf.getString(AzkabanConstant.AZKABAN_UPLOAD_CMD)
                .replace("{PROJECTNAME}", projectName)
                .replace("{SESSIONID}", sessionId)
                .replace("{PROJECTZIPFILE}", projectZipFile)
                .replace("{IP}", baseConf.getString(AzkabanConstant.AZKABAN_IP))
                .replace("{PORT}", baseConf.getString(AzkabanConstant.AZKABAN_PORT));
        String execString = exector.exec(uploadCommand);
        JSONObject resJosn = JSON.parseObject(execString.substring(execString.indexOf("{")));
        String result = "";
        if (resJosn.containsKey("error"))
            result = String.format("【upload_error】error_message:%s", resJosn.getString("error"));
        else
            result = String.format("【upload_success】projectId:%s,version:%s", resJosn.getString("projectId"), resJosn.getString("version"));
        return result;
    }

    private String createProject(String sessionId, String projectName, Config baseConf) {
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
        JSONObject resJosn = JSON.parseObject(execString.substring(execString.indexOf("{")));
        String status = resJosn.getString("status");
        log.debug("【status】" + status);
        if (status.trim().equals("error")) {
            log.error("【create_error】" + resJosn.getString("message"));
        }
        return status;

    }

    public static void main(String[] args) {
        Config baseConf = ConfigFactory.load(AzkabanConstant.AKABAN_CONIF_FILE_NAME).getConfig(AzkabanConstant.ENV_AZKABAN);
        AzkabanHelper h = new AzkabanHelper();
        String sessionId = h.getAzkabanSessionId(baseConf);
        h.createProject(sessionId, "test_lyh_a", baseConf);

    }

}
