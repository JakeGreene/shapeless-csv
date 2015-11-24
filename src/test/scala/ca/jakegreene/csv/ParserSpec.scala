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
    
    "parse multiple lines of Bytes" in {
      val parsedBytes = CsvParser.parse[Byte]("1\n2\n3")
      parsedBytes should contain allOf(Right(1), Right(2), Right(3))
    }
    
    "fail to parse invalid data into Bytes" in {
      val parsedBytes = CsvParser.parse[Byte]("a\n128\n1,2")
      parsedBytes should contain allOf (Left(s"Cannot parse [a] to Byte"),
                                        Left(s"Cannot parse [128] to Byte"),
                                        Left(s"Cannot parse [1,2] to Byte"))
    }
    
    "parse multiple lines of Shorts" in {
      val parsedShorts = CsvParser.parse[Short]("1\n2\n3")
      parsedShorts should contain allOf (Right(1), Right(2), Right(3))
    }
    
    "fail to parse invalid data into Shorts" in {
      val parsedShorts = CsvParser.parse[Short]("a\n32768\n1,2")
      parsedShorts should contain allOf (Left(s"Cannot parse [a] to Short"),
                                         Left(s"Cannot parse [32768] to Short"),
                                         Left(s"Cannot parse [1,2] to Short"))
    }
    
    "parse multiple lines of Ints" in {
      val parsedInts = CsvParser.parse[Int]("1\n2\n3")
      parsedInts should contain allOf (Right(1), Right(2), Right(3))
    }
    
    "fail to parse invalid data into Ints" in {
      val parsedInts = CsvParser.parse[Int]("a\n2147483648\n1,2")
      parsedInts should contain allOf (Left(s"Cannot parse [a] to Int"),
                                       Left(s"Cannot parse [2147483648] to Int"),
                                       Left(s"Cannot parse [1,2] to Int"))
    }
    
    "parse multiple lines of Longs" in {
      val parsedLongs = CsvParser.parse[Long]("1\n2\n300000000000000")
      parsedLongs should contain allOf (Right(1L), Right(2L), Right(300000000000000L))
    }
    
    "fail to parse invalid data into Longs" in {
      val parsedLongs = CsvParser.parse[Long]("a\n9223372036854775808\n1,2")
      parsedLongs should contain allOf (Left(s"Cannot parse [a] to Long"),
                                        Left(s"Cannot parse [9223372036854775808] to Long"),
                                        Left(s"Cannot parse [1,2] to Long"))
    }
    
    "parse multiple lines of Strings" in {
      val parsedStrings = CsvParser.parse[String]("hello\nworld\nmoon")
      parsedStrings should contain allOf (Right("hello"), Right("world"), Right("moon"))
    }
    
    "parse multiple lines of Chars" in {
      val parsedChars = CsvParser.parse[Char]("a\nb\nc")
      parsedChars should contain allOf(Right('a'), Right('b'), Right('c'))
    }
    
    "fail to parse invalid data into Chars" in {
      val parsedChars = CsvParser.parse[Char]("abc\n123\na,b")
      parsedChars should contain allOf (Left(s"Cannot parse [abc] to Char"),
                                        Left(s"Cannot parse [123] to Char"),
                                        Left(s"Cannot parse [a,b] to Char"))
    }
    
    "parse multiple lines of Floats" in {
      val parsedFloats = CsvParser.parse[Float]("3.14\n2.22\n0.11")
      parsedFloats should contain allOf (Right(3.14f), Right(2.22f), Right(0.11f))
    }
    
    "fail to parse invalid data into Floats" in {
      val parsedFloats = CsvParser.parse[Float]("a\nxyz\n1,2")
      parsedFloats should contain allOf (Left(s"Cannot parse [a] to Float"),
                                         Left(s"Cannot parse [xyz] to Float"),
                                         Left(s"Cannot parse [1,2] to Float"))
    }
    
    "parse multiple lines of Doubles" in {
      val parsedDoubles = CsvParser.parse[Double]("3.14\n2.22\n0.11")
      parsedDoubles should contain allOf (Right(3.14), Right(2.22), Right(0.11))
    }
    
    "fail to parse invalid data into Doubles" in {
      val parsedDoubles = CsvParser.parse[Double]("a\nxyz\n1,2")
      parsedDoubles should contain allOf (Left(s"Cannot parse [a] to Double"),
                                          Left(s"Cannot parse [xyz] to Double"),
                                          Left(s"Cannot parse [1,2] to Double"))
    }
    
    "parse multiple lines of Booleans" in {
      val parsedBooleans = CsvParser.parse[Boolean]("true\nfalse")
      parsedBooleans should contain allOf (Right(true), Right(false))
    }
    
    "fail to parse invalid data into Booleans" in {
      val parsedBooleans = CsvParser.parse[Boolean]("a\n1\ntrue,false")
      parsedBooleans should contain allOf (Left(s"Cannot parse [a] to Boolean"),
                                           Left(s"Cannot parse [1] to Boolean"),
                                           Left(s"Cannot parse [true,false] to Boolean"))
    }
    
    "parse multiple lines into case classes" in {
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
      implicit val longParser = CsvParser.headInstance(s => Try(s.toLong + 3).toOption)(s => "Could not parse [$s] to Long")
      val parsedLongs = CsvParser.parse[Long]("1234567890")
      parsedLongs.size should equal (1)
      val parsedLong = parsedLongs(0)
      parsedLong.right.get should equal (1234567890 + 3)
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