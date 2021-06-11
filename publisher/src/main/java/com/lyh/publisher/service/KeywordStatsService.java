package com.lyh.publisher.service;

import com.lyh.publisher.bean.KeywordStats;

import java.util.List;

public interface KeywordStatsService {
    public List<KeywordStats> getKeywordStats(int date, int limit);
}
