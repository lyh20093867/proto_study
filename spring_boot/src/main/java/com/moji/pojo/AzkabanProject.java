package com.moji.pojo;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@Data
public class AzkabanProject {
    @Column("id")
    private Integer id;
    private String projectName;
    private String projectExecFrequency;
    private Integer step;
    private String lastSuccExecId;
    private String lastSuccExecBeginDt;
    private String selfActive;
    private String selfBeginDt;
    private String selfEndDt;
    private Integer updaterId;
    private String updateTime;
    private int status;
    private String entryHome;
    private String agentHome;
}
