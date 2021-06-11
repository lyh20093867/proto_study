package com.lyh.publisher.service;

import com.lyh.publisher.bean.VisitorStats;

import java.util.List;

public interface VisitorStatsService {
    List<VisitorStats> getVisitorStatsByNewFlag(int date);

    List<VisitorStats> getVisitorStatsByHr(int date);
}
