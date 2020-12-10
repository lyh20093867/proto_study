package com.lyh.proto.kafkfa

import java.util.Properties

import com.moji.protobuffer.CommonLogAndroidProtos
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}

object KafkaProtoTest {
  def main(args: Array[String]): Unit = {
    val properties = new Properties()
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.16.19.15:9092,172.16.19.16:9092,172.16.19.17:9092,172.16.19.18:9092,172.16.19.19:9092,172.16.19.20:9092")
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.BytesSerializer")

    val producer = new KafkaProducer[String, Array[Byte]](properties)
    val message = CommonLogAndroidProtos.CommonLogAndroid.newBuilder()
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
    val bytes = message.build().toByteArray
    producer.send(new ProducerRecord[String, Array[Byte]]("test_proto_lyh", bytes))
  }
}
