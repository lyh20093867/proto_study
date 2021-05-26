package com.moji.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
public class JobDataRel {
    String jobId;
    String jobType;
}
