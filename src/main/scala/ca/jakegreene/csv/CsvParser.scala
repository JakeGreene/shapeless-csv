package ca.jakegreene.csv

import scala.util.Try
import shapeless._
import shapeless.ops.hlist._

trait CsvParser[T] {
  def apply(cells: Seq[String]): CsvParser.ParseResult[T] = parse(cells)
  def parse(cells: Seq[String]): CsvParser.ParseResult[T]
  def size: Int
}

object CsvParser {

  type ParseResult[T] = Either[String, T]

  def parse[T](s: String)(implicit parser: CsvParser[T]): Seq[ParseResult[T]] = {
    val lines = s.split("\n").toList
    lines.map { line =>
      val cells = line.split(",").toList
      parser(cells)
    }
  }

  def instance[T](s: Int)(p: Seq[String] => ParseResult[T]): CsvParser[T] = new CsvParser[T] {
    def parse(cells: Seq[String]): ParseResult[T] = {
      if (cells.length == s) {
        p(cells)
      } else {
        Left(s"Input size [${cells.length}] does not match parser expected size [$s]")
      }
    }
    def size = s
  }

  def headInstance[T](p: String => Option[T])(failure: String => String): CsvParser[T] = new CsvParser[T] {
    def parse(cells: Seq[String]): ParseResult[T] = {
      cells match {
        case head +: Nil =>
          val maybeParsed = p(head)
          maybeParsed.toRight(failure(head))
        case _ =>
          Left(failure(cells.mkString(",")))
      }
    }
    def size = 1
  }

  implicit val stringParser = primitiveParser(identity)
  implicit val charParser = primitiveParser { s =>
    if (s.length == 1) s.head
    else throw new IllegalArgumentException("Char Parser requires strings of size one")
  }
  implicit val byteParser = primitiveParser(_.toByte)
  implicit val shortParser = primitiveParser(_.toShort)
  implicit val intParser = primitiveParser(_.toInt)
  implicit val longParser = primitiveParser(_.toLong)
  implicit val floatParser = primitiveParser(_.toFloat)
  implicit val doubleParser = primitiveParser(_.toDouble)
  implicit val booleanParser = primitiveParser(_.toBoolean)

  def primitiveParser[P](parse: String => P)(implicit typ: Typeable[P]): CsvParser[P] = {
    headInstance(s => Try(parse(s)).toOption)(s => s"Cannot parse [$s] to ${typ.describe}")
  } 

  /*
   * Base Case: given an empty sequence of cells, produce an HNil
   */
  implicit val hnilParser: CsvParser[HNil] = instance(0)(s => Right((HNil)))

  /*
   * Inductive Step: given a sequence of cells, parse the front cells into `Head` and the sequence of remaining cells into `Tail`.
   * `Head` is not limited to one cell; `Head` may be a class requiring multiple cells.
   *
   */
  implicit def hconsParser[Head, Tail <: HList](implicit hp: Lazy[CsvParser[Head]], tp: Lazy[CsvParser[Tail]]): CsvParser[Head :: Tail] = {
    instance(hp.value.size + tp.value.size) { cells =>
      // Compiler bug SI-7222 prevents this from being a for-comprehension
      val (headCells, tailCells) = cells.splitAt(hp.value.size)
      hp.value.parse(headCells).right.flatMap { case (head) =>
        tp.value.parse(tailCells).right.map { case (tail) =>
          (head :: tail)
        }
      }
    }
  }

  /**
   * A parser for a case class that has a parser for it's HList representation
   */
  implicit def caseClassParser[Case, Repr <: HList](implicit gen: Generic.Aux[Case, Repr], reprParser: Lazy[CsvParser[Repr]]): CsvParser[Case] = {
    instance(reprParser.value.size) { cells =>
      reprParser.value.parse(cells).right.map { parsed =>
        (gen.from(parsed))
      }
    }
  }

}