package com.lyh.publisher.mapper;

import com.lyh.publisher.bean.KeywordStats;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface KeywordStatsMapper {
    @Select("select keyword," +
            "sum(keyword_stats_200821.ct * " +
            "multiIf(source='SEARCH',10,source='ORDER',3,source='CART',2,source='CLICK',1,0)) ct" +
            " from keyword_stats_200821 where toYYYYMMDD(stt)=#{date} group by keyword " +
            "order by sum(keyword_stats_200821.ct) desc limit #{limit} ")
    public List<KeywordStats> selectKeywordStats(@Param("date") int date, @Param("limit") int limit);
}
