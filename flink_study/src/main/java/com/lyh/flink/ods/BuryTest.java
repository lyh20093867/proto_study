package com.lyh.flink.ods;

import com.lyh.flink.utils.MyKafkaUtils;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.ArrayList;

public class BuryTest {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setMaxParallelism(120);
        ArrayList topics = new ArrayList();
        topics.add("bury_feeds");
        FlinkKafkaConsumer source = MyKafkaUtils.getKafkaSource(topics, "bury_test_lyh");
        DataStreamSource kafkaDs = env.addSource(source);
        kafkaDs.print();
        env.execute();
    }
}
