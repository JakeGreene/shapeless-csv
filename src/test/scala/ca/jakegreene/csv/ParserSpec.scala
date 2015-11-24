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
      val parsedCaseClasses = CsvParser.parse[Test]("hello world,12345,3.14")
      parsedCaseClasses.size should equal (1)
      val parsedCaseClass = parsedCaseClasses(0)
      parsedCaseClass.right.get.s should equal ("hello world")
      parsedCaseClass.right.get.i should equal (12345)
      parsedCaseClass.right.get.d should equal (3.14)
    }
    
    "fail to parse case class when given too few arguments" in {
      val parsedCaseClasses = CsvParser.parse[Test]("hello world,12345")
      parsedCaseClasses.size should equal (1)
      val parsedCaseClass = parsedCaseClasses(0)
      parsedCaseClass shouldBe a [Left[_, _]]
    }
    
    "fail to parse case class when given too many arguments" in {
      val parsedCaseClasses = CsvParser.parse[Test]("hello world,12345,3.14,54321")
      parsedCaseClasses.size should equal (1)
      val parsedCaseClass = parsedCaseClasses(0)
      parsedCaseClass shouldBe a [Left[_, _]]
    }
    
    "fail to parse case class when given incorrect types" in {
      val parsedCaseClasses = CsvParser.parse[Test]("hello,12345,world")
      parsedCaseClasses.size should equal (1)
      val parsedCaseClass = parsedCaseClasses(0)
      parsedCaseClass shouldBe a [Left[_, _]]
    }
    
    "fail to compile when given invalid type" in {
      """val parsedCaseClass = Parser.parse[Invalid]("hello world")""" shouldNot compile
    }
    
    "parse multiple lines of Ints" in {
      val parsedInts = CsvParser.parse[Int]("1\n2\n3")
      parsedInts should contain allOf (Right(1), Right(2), Right(3))
    }
    
    "parse multiple lines of Strings" in {
      val parsedStrings = CsvParser.parse[String]("hello\nworld\nmoon")
      parsedStrings should contain allOf (Right("hello"), Right("world"), Right("moon"))
    }
    
    "parse multiple lines of Doubles" in {
      val parsedDoubles = CsvParser.parse[Double]("3.14\n2.22\n0.11")
      parsedDoubles should contain allOf (Right(3.14), Right(2.22), Right(0.11))
    }
    
    "parse multiple case class" in {
      case class Test(a: Int, b: Double)
      val parsedTests = CsvParser.parse[Test]("3,0.14\n1,0.45")
      parsedTests should contain allOf (Right(Test(3, 0.14)), Right(Test(1, 0.45)))
    }
    
    "partially parse a CSV with different types of lines" in {
      val parsedCsv = CsvParser.parse[Int]("1\nhello\n3.14")
      parsedCsv should contain (Right(1))
      parsedCsv should contain (Left(s"Cannot parse [hello] to Int"))
      parsedCsv should contain (Left(s"Cannot parse [3.14] to Int"))
    }
    
    "parse if given an implicit" in {
      implicit val longParser = CsvParser.headInstance(s => Try(s.toLong).toOption)(s => "Could not parse [$s] to Long")
      val parsedLongs = CsvParser.parse[Long]("1234567890")
      parsedLongs.size should equal (1)
      val parsedLong = parsedLongs(0)
      parsedLong.right.get should equal (1234567890)
    }
    
    "parse nested case classes" in {
      case class Holder(t: Test)
      val parsedHolders = CsvParser.parse[Holder]("hello world,54321,3.14")
      parsedHolders.size should equal (1)
      val parsedHolder = parsedHolders(0)
      parsedHolder should equal (Right(Holder(Test("hello world", 54321, 3.14))))
    }
    
    "parse padded nested case class with header" in {
      case class Holder(header: Int, t: Test)
      val parsedHolders = CsvParser.parse[Holder]("3,hello world,54321,3.14")
      parsedHolders.size should equal (1)
      val parsedHolder = parsedHolders(0)
      parsedHolder should equal (Right(Holder(3, Test("hello world", 54321, 3.14))))
    }
    
    "parse padded nested case class with footer" in {
      case class Holder(t: Test, footer: Int)
      val parsedHolders = CsvParser.parse[Holder]("hello world,54321,3.14,3")
      parsedHolders.size should equal (1)
      val parsedHolder = parsedHolders(0)
      parsedHolder should equal (Right(Holder(Test("hello world", 54321, 3.14), 3)))
    }
    
    "parse padded nested case class with header/footer" in {
      case class Holder(header: Int, t: Test, footer: Int)
      val parsedHolders = CsvParser.parse[Holder]("4,hello world,54321,3.14,3")
      parsedHolders.size should equal (1)
      val parsedHolder = parsedHolders(0)
      parsedHolder should equal (Right(Holder(4, Test("hello world", 54321, 3.14), 3)))
    }
  }
}