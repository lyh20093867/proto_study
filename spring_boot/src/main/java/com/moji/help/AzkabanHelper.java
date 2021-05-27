package com.moji.help;

import ch.qos.logback.core.util.LocationUtil;
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
        String sourceDirPath = project.getEntryHome();
        File sourceZipDir = new File(sourceDirPath);
        if (sourceZipDir.exists()) {
            String rmCommand = String.format("rm -r %s", sourceDirPath);
            log.warn("【rmCommand】" + rmCommand);
            exector.exec(rmCommand);
        }
        sourceZipDir.mkdir();
        generateCommonParamFile(sourceZipDir, project);
        recursionPreJob(preJob, sourceZipDir, project);
        File finalJobDir = new File("/Users/yihong.li/Documents/work/csv/web_app_final");
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
                    log.warn("【timer_trigger_command】" + timer_trigger_command);
                    bw.write(timer_trigger_command);
                } else {
                    String jobCommand = String.format("command=sh +x %s/entry_sh.sh %s %s %s/agent.sh %s %s  ${azkaban.flow.projectid} ${azkaban.flow.flowid} ${azkaban.flow.execid}",
                            project.getEntryHome(), job.getServerLocation(), project.getAgentHome(), job.getPathLocation(), jobName);
                    log.warn(String.format("【%s_jobCommand】%s", jobName, jobCommand));
                    bw.write(jobCommand);
                }
                bw.newLine();
                String dep = job.getDependencies();
                if (dep != null && dep != "") {
                    String depStr = String.format("dependencies=%s", dep);
                    log.warn(String.format("【%s_dependencies】%s", jobName, depStr));
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
        String projectZipFile = generateProjectZipFile(project, preJob);
        Config baseConf = ConfigFactory.load(AzkabanConstant.AKABAN_CONIF_FILE_NAME);
        String sessionId = getAzkabanSessionId(baseConf);
        createProject(sessionId, project.getProjectName(), baseConf);
        return uploadProjectFile(sessionId, projectZipFile, project.getProjectName(), baseConf);
    }

    private String getAzkabanSessionId(Config baseConf) {
        String loginCommand = baseConf.getString(AzkabanConstant.AZKABAN_CREATE_PROJECT_CMD)
                .replace("{USER}", baseConf.getString(AzkabanConstant.AZKABAN_USER))
                .replace("{PASSWORD}", baseConf.getString(AzkabanConstant.AZKABAN_PASSWORD))
                .replace("{IP}", baseConf.getString(AzkabanConstant.AZKABAN_IP))
                .replace("{PORT}", baseConf.getString(AzkabanConstant.AZKABAN_PORT));
        log.warn(String.format("【%s_loginCommand】%s", loginCommand));
        return exector.exec(loginCommand);
    }

    private String uploadProjectFile(String sessionId, String projectZipFile, String projectName, Config baseConf) {
        String uploadCommand = baseConf.getString(AzkabanConstant.AZKABAN_UPLOAD_CMD)
                .replace("{PROJECTNAME}", projectName)
                .replace("{SESSIONID}", sessionId)
                .replace("{PROJECTZIPFILE}", projectZipFile)
                .replace("{IP}", baseConf.getString(AzkabanConstant.AZKABAN_IP))
                .replace("{PORT}", baseConf.getString(AzkabanConstant.AZKABAN_PORT));
        log.warn(String.format("【%s_uploadCommand】%s", projectName, uploadCommand));
        return exector.exec(uploadCommand);
    }

    private String createProject(String sessionId, String projectName, Config baseConf) {
        String createCommand = baseConf.getString(AzkabanConstant.AZKABAN_CREATE_PROJECT_CMD)
                .replaceAll("\\{PROJECTNAME\\}", projectName)
                .replace("{SESSIONID}", sessionId)
                .replace("{IP}", baseConf.getString(AzkabanConstant.AZKABAN_IP))
                .replace("{PORT}", baseConf.getString(AzkabanConstant.AZKABAN_PORT));
        log.warn(String.format("【%s_createCommand】%s", projectName, createCommand));
        return exector.exec(createCommand);
    }

}
