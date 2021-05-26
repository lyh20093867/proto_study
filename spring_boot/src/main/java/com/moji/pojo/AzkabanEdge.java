package com.moji.pojo;

import lombok.Data;

@Data
public class AzkabanEdge {
    String fromJobId;
    String fromJobName;
    String toJobId;
    String toJobName;
}
