package com.lyh.publisher.service;

import com.lyh.publisher.bean.ProvinceStats;

import java.util.List;

public interface ProvinceStatsService {
    List<ProvinceStats> getProvinceStats(int date);
}
