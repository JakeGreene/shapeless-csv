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
      val parsedCaseClass = Parser.parse[Test]("hello world,12345,3.14")
      parsedCaseClass.right.get.s should equal ("hello world")
      parsedCaseClass.right.get.i should equal (12345)
      parsedCaseClass.right.get.d should equal (3.14)
    }
    
    "fail to parse case class when given too few arguments" in {
      val parsedCaseClass = Parser.parse[Test]("hello world,12345")
      parsedCaseClass shouldBe a [Left[_, _]]
    }
    
    "fail to parse case class when given too many arguments" in {
      val parsedCaseClass = Parser.parse[Test]("hello world,12345,3.14,54321")
      parsedCaseClass shouldBe a [Left[_, _]]
    }
    
    "fail to parse case class when given incorrect types" in {
      val parsedCaseClass = Parser.parse[Test]("hello,12345,world")
      parsedCaseClass shouldBe a [Left[_, _]]
    }
    
    "fail to compile when given invalid type" in {
      """val parsedCaseClass = Parser.parse[Invalid]("hello world")""" shouldNot compile
    }
    
    "parse a CSV int into an Int" in {
      val pasedInt = Parser.parse[Int]("12345")
      pasedInt.right.get should equal (12345)
    }
    
    "parse a CSV string into a String" in {
      val parsedString = Parser.parse[String]("Hello World")
      parsedString.right.get should equal ("Hello World")
    }
    
    "parse a CSV double into a Double" in {
      val parsedDouble = Parser.parse[Double]("3.14")
      parsedDouble.right.get should equal (3.14)
    }
    
    "parse if given an implicit" in {
      implicit val longParser = Parser.headInstance(s => Try(s.toLong).toOption)(s => "Could not parse [$s] to Long")
      val parsedLong = Parser.parse[Long]("1234567890")
      parsedLong.right.get should equal (1234567890)
    }
    
    "parse nested case classes" in {
      case class Holder(t: Test)
      val parsedHolder = Parser.parse[Holder]("hello world,54321,3.14")
      parsedHolder should equal (Right(Holder(Test("hello world", 54321, 3.14))))
    }
    
    "parse padded nested case class with header" in {
      case class Holder(header: Int, t: Test)
      val parsedHolder = Parser.parse[Holder]("3,hello world,54321,3.14")
      parsedHolder should equal (Right(Holder(3, Test("hello world", 54321, 3.14))))
    }
    
    "parse padded nested case class with footer" in {
      case class Holder(t: Test, footer: Int)
      val parsedHolder = Parser.parse[Holder]("hello world,54321,3.14,3")
      parsedHolder should equal (Right(Holder(Test("hello world", 54321, 3.14), 3)))
    }
    
    "parse padded nested case class with header/footer" in {
      case class Holder(header: Int, t: Test, footer: Int)
      val parsedHolder = Parser.parse[Holder]("4,hello world,54321,3.14,3")
      parsedHolder should equal (Right(Holder(4, Test("hello world", 54321, 3.14), 3)))
    }
  }
}