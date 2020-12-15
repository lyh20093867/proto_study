package com.lyh.proto.serialization

import com.moji.protobuffer.{TestProtos}
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import java.util

import com.google.protobuf.InvalidProtocolBufferException

class TestSerializer extends Deserializer[TestProtos.Test] {

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit = {}

  override def deserialize(topic: String, data: Array[Byte]): TestProtos.Test = {
    try {
      TestProtos.Test.parseFrom(data)
    } catch {
      case e: InvalidProtocolBufferException => throw new SerializationException("Error when deserializing byte[] to string due to unsupported encoding !")
    }
  }

  override def close(): Unit = {}
}
