package com.lyh.proto.serialization

import java.util

import com.moji.protobuffer.TestProtos
import org.apache.kafka.common.serialization.Serializer

class TestDeSerializer extends Serializer[TestProtos.Test] {
  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def serialize(topic: String, data: TestProtos.Test): Array[Byte] = {
    if (data == null) null else data.toByteArray
  }
  override def close(): Unit = {}
}
