package com.lyh.proto.kafkfa

import java.util.Properties
import org.apache.kafka.clients.producer.ProducerConfig
object KafkaProtoTest {
  def main(args: Array[String]): Unit = {
    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"172.16.19.15:9092,172.16.19.16:9092,172.16.19.17:9092,172.16.19.18:9092,172.16.19.19:9092,172.16.19.20:9092")
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"172.16.19.15:9092,172.16.19.16:9092,172.16.19.17:9092,172.16.19.18:9092,172.16.19.19:9092,172.16.19.20:9092")
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"172.16.19.15:9092,172.16.19.16:9092,172.16.19.17:9092,172.16.19.18:9092,172.16.19.19:9092,172.16.19.20:9092")
    properties.put(ProducerConfig.SCHEMA_REGISTRY_URL_CONFIG,"172.16.19.15:9092,172.16.19.16:9092,172.16.19.17:9092,172.16.19.18:9092,172.16.19.19:9092,172.16.19.20:9092")

  }
}
