package ca.jakegreene.csv

import org.scalatest.Matchers
import org.scalatest.WordSpec
import org.scalatest.OptionValues
import scala.util.Try

trait Invalid
case class Test(s: String, i: Int, d: Double)

class ParserSpec extends WordSpec with Matchers with OptionValues {
  "A Parser" should {
    "parse a single CSV line into a case class" in {
      val maybeCaseClass = Parser.parse[Test]("hello world,12345,3.14")
      maybeCaseClass.value.s should equal ("hello world")
      maybeCaseClass.value.i should equal (12345)
      maybeCaseClass.value.d should equal (3.14)
    }
    
    "fail to parse case class when incorrect number of arguments" in {
      val maybeCaseClass = Parser.parse[Test]("hello world,12345")
      maybeCaseClass should be (None)
    }
    
    "fail to parse case class when incorrect types" in {
      val maybeCaseClass = Parser.parse[Test]("hello,12345,world")
      maybeCaseClass should be (None)
    }
    
    "fail to compile when given invalid type" in {
      """val maybeClass = Parser.parse[Invalid]("hello world")""" shouldNot compile
    }
    
    "parse a CSV int into an Int" in {
      val maybeInt = Parser.parse[Int]("12345")
      maybeInt.value should equal (12345)
    }
    
    "parse a CSV string into a String" in {
      val maybeString = Parser.parse[String]("Hello World")
      maybeString.value should equal ("Hello World")
    }
    
    "parse a CSV double into a Double" in {
      val maybeDouble = Parser.parse[Double]("3.14")
      maybeDouble.value should equal (3.14)
    }
    
    "parse if given an implicit" in {
      implicit val longParser = Parser.instance(s => Try(s.toLong).toOption)
      val maybeLong = Parser.parse[Long]("1234567890")
      maybeLong.value should equal (1234567890)
    }
  }
}