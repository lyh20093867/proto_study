package com.lyh.proto.kafkfa

import java.util.Properties

import com.lyh.proto.serialization.TestSerializer
import com.moji.protobuffer.{CommonLogAndroidProtos, TestProtos}
import com.moji.protobuffer.CommonLogAndroidProtos.CommonLogAndroid
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.{BytesDeserializer, StringDeserializer, StringSerializer}

object KafkaProtoTest {
  def main(args: Array[String]): Unit = {
    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.19.15:9092,172.16.19.16:9092,172.16.19.17:9092,172.16.19.18:9092,172.16.19.19:9092,172.16.19.20:9092")
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer])
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[BytesDeserializer])
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[TestSerializer])
    // 生产数据
    produce(properties)
    // 消费数据
    comsume(properties)
  }

  private def comsume(properties: Properties) = {
    val consumer = new KafkaConsumer[String, TestProtos.Test](properties)
    import scala.collection.JavaConversions._
    consumer.subscribe(Array("test_proto_lyh").toList)
    var i = 0
    while (i < 100000) {
      consumer.poll(1000).foreach(record => {
        val mess = record.value()
        println(s"tp:${record.topic()},key:${record.key()},uid:${mess.getUid},age:${mess.getAge}")
      })
      Thread.sleep(100)
      i += 1
    }
    consumer.close()
  }

  private def produce(properties: Properties) = {
    val producer = new KafkaProducer[String, TestProtos.Test](properties)
    Range(1, 100).foreach(e => {
      println("uid:" + e)
      producer.send(new ProducerRecord[String, TestProtos.Test]("test_proto_lyh", setTest("uid" + e, e)))
      println("uid2:" + e)
    })
    producer.flush()
    producer.close()
  }

  def setTest(): TestProtos.Test.Builder = {
    val test = TestProtos.Test.newBuilder()
    test.setUid("1231")
    test.setAge(1)
    test
  }

  def setTest(uid: String, age: Int): TestProtos.Test = {
    TestProtos.Test.newBuilder().setUid(uid).setAge(age).build()
  }

  def setCommonLogAndroid(): CommonLogAndroid.Builder = {
    val message = CommonLogAndroid.newBuilder()
    val loc = CommonLogAndroidProtos.Location.newBuilder()
    val param = CommonLogAndroidProtos.Params.newBuilder()
    val common = CommonLogAndroidProtos.Common.newBuilder()
    val device = CommonLogAndroidProtos.Common.Device.newBuilder()
    val user = CommonLogAndroidProtos.Common.User.newBuilder()
    device.setAppVersion("5008050606")
    device.setBrand("apple")
    device.setDevice("iPhone9,2")
    device.setHeight("1792")
    device.setIp("192.168.2.162")
    device.setLanguage("CN")
    device.setMcc("460")
    device.setMnc("00")
    device.setNet("wifi")
    device.setNumber("13218324638")
    device.setWidth("1068")
    device.setOsVersion("13.6")
    device.setOaid("366053604300554240")
    device.setType(CommonLogAndroidProtos.Common.PlatformType.IOS)
    common.setUid("696968898")
    common.setDevice(device)
    user.setSnsid("a")
    user.setUid("1231321")
    user.setVip("0")
    common.setUid("1231321")
    common.setUser(user)
    loc.setCdmalat("a")
    loc.setCdmalon("b")
    loc.setCid("c")
    loc.setCityid("121")
    loc.setLac("12312")
    loc.setLat("test")
    loc.setLbstype("gps")
    loc.setLon("12")
    loc.setType(CommonLogAndroidProtos.LbsType.BEIDOU)
    param.setProp1("aaaa")
    message.setCommon(1, common)
    message.setLoc(2, loc)
    message.setParam(3, param)
    message
  }
}
