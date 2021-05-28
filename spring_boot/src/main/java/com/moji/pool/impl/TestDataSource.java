package com.moji.pool.impl;

import com.moji.pool.AbstractDataSource;
import org.springframework.context.annotation.Bean;


public class TestDataSource extends AbstractDataSource {
    private String url = "jdbc:mysql://192.168.9.55:3306/moji_dam?useUnicode=true&characterEncoding=utf8";
    public TestDataSource(){
        initSource();
    }
    @Override
    protected void initSource() {
        bigDataMojiDataSource.setJdbcUrl(url);
        bigDataMojiDataSource.setUsername("root");
        bigDataMojiDataSource.setMaximumPoolSize(20);
        bigDataMojiDataSource.setMinimumIdle(5);
    }
}
