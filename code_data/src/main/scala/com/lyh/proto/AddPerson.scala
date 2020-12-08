package com.lyh.proto

import java.io.{BufferedReader, FileInputStream, FileNotFoundException, FileOutputStream, InputStreamReader, PrintStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.moji.protobuffer.AddressBookProtos.{AddressBook, Person}

/** *
 *
 * @description: 对数据进行输入和输出，并通过protobuffer的方式进行传输，使用gzip的方式进行压缩
 * @author:lyh
 * @date:2019 /12/6
 * @version:
 */
object AddPerson {
  def promptForAddress(stdin: BufferedReader, stdout: PrintStream): Person = {
    val person = Person.newBuilder()
    stdout.print("Enter person Id:")
    person.setId(Integer.valueOf(stdin.readLine()))

    stdout.print("Enter name: ")
    person.setName(stdin.readLine)

    stdout.print("Enter email address (blank for none): ")
    val email = stdin.readLine
    if (email.length > 0) person.setEmail(email)
    var number = "0"
    while (number.length > 0) {
      stdout.print("Enter a phone number (or leave blank to finish): ")
      number = stdin.readLine()
      if (number != null && number.trim != "") {
        println(s"number:$number")
        val phoneNumber = Person.PhoneNumber.newBuilder().setNumber(number)
        stdout.print("Is this a mobile, home, or work phone? ")
        val pType = stdin.readLine
        println(s"pType:$pType")
        if (pType == "mobile") phoneNumber.setType(Person.PhoneType.MOBILE)
        else if (pType == "home") phoneNumber.setType(Person.PhoneType.HOME)
        else if (pType == "work") phoneNumber.setType(Person.PhoneType.WORK)
        else stdout.println("Unknown phone type.  Using default.")
        person.addPhones(phoneNumber)
      }

    }
    person.build()
  }

  def printPerson(addressBook: AddressBook): Unit = {
    import scala.collection.JavaConverters._
    addressBook.getPeopleList.asScala.foreach(person => {
      println("Person ID: " + person.getId())
      println("  Name: " + person.getName())
      println("  E-mail address: " + person.getEmail())
      person.getPhonesList.asScala.foreach(phone => {
        phone.getType match {
          case Person.PhoneType.MOBILE => print("  Mobile phone #: ")
          case Person.PhoneType.HOME => print("  HOME phone #: ")
          case Person.PhoneType.WORK => print("  WORK phone #: ")
          case _ => print("  qita phone #: ")
        }
        println(phone.getNumber)
      })
    })
  }

  def main(args: Array[String]): Unit = {
    //    write
    read
  }

  private def read = {
    val input = new FileInputStream("/Users/yihong.li/Documents/project/proto_study/code_data/src/data/proto_test_lyh")
    val gzipInput = new GZIPInputStream(input)
    printPerson(AddressBook.parseFrom(gzipInput))
  }

  /**
   * 此处在调用了close方法后，再调用读流的方法，会导致Exception: Unexpected end of ZLIB input stream的异常
   */
  //  private def write = {
  //    val addressBook = AddressBook.newBuilder()
  //    try {
  //      addressBook.mergeFrom(new FileInputStream("/Users/yihong.li/Documents/project/proto_study/code_data/src/data/proto_test_lyh"))
  //    } catch {
  //      case e: FileNotFoundException => println(": /Users/yihong.li/Documents/project/proto_study/code_data/src/data/proto_test_lyh not found.  Creating a new file.")
  //    }
  //    addressBook.addPeople(promptForAddress(new BufferedReader(new InputStreamReader(System.in)), System.out))
  //    val output = new FileOutputStream("/Users/yihong.li/Documents/project/proto_study/code_data/src/data/proto_test_lyh")
  //    val gzipoutput = new GZIPOutputStream(output)
  //    addressBook.build().writeTo(gzipoutput)
  //    output.close()
  //  }
  /**
   *下面为正确的做法
   */
  private def write = {
    val addressBook = AddressBook.newBuilder()
    try {
      addressBook.mergeFrom(new FileInputStream("/Users/yihong.li/Documents/project/proto_study/code_data/src/data/proto_test_lyh"))
    } catch {
      case e: FileNotFoundException => println(": /Users/yihong.li/Documents/project/proto_study/code_data/src/data/proto_test_lyh not found.  Creating a new file.")
    }
    addressBook.addPeople(promptForAddress(new BufferedReader(new InputStreamReader(System.in)), System.out))
    val gzipoutput = new GZIPOutputStream(new FileOutputStream("/Users/yihong.li/Documents/project/proto_study/code_data/src/data/proto_test_lyh"))
    addressBook.build().writeTo(gzipoutput)
    gzipoutput.close()
  }
}
