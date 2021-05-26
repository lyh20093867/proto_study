package com.moji.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;

@Data
public class AzkabanJob {
    private @Id
    Long id;
    @Column("name")
    private String jobName;
    @Column("job_type_cid")
    private String jobTypeId;
    private String jobTypeName;
    private String execType;
    private String serverId;
    private String serverLocation;
    private String serverName;
    @Column("path_locationid")
    private String pathId;
    private String pathLocation;
    private int out;
    private int in;
    private int color;


    private String dependencies;

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        return prime * result + id.hashCode() + jobName.hashCode() + jobTypeName.hashCode();
    }

    @Override
    public String toString() {
        return String.format("AzkabanJob{id:%s,jobName:%s,dependencies:%s,color:%s}", id, jobName, dependencies, color);
    }
}
