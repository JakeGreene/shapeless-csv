package ca.jakegreene.csv

import scala.util.Try
import shapeless._
import shapeless.ops.hlist._

trait CsvParser[T] {
  import CsvParser.{Cell, ParseResult}
  def apply(cells: Seq[Cell]): ParseResult[T] = parse(cells)
  def parse(cells: Seq[Cell]): ParseResult[T]
  /**
   * The number of `Cell`s this `CsvParser` needs to consume to produce a `T`
   */
  def size: Int
}

object CsvParser {

  type Cell = String
  type ParseResult[T] = Either[String, T]

  /**
   * Attempt to parse the CSV string `csv` into a `Seq[T]`
   * 
   * Example usage:
   * {{{
   * // Produces Right(3)
   * CsvParser.parse[Int]("3")
   * 
   * case class Sample(i: Int)
   * // Produces Right(Sample(3))
   * CsvParser.parse[Sample]("3")
   * }}}
   * 
   * @param csv A string representation of a CSV. Each cell must be divided by a comma and each line must be separated by a newline
   * @param hasHeader Does `csv` have a header? Defaults to false (i.e. no)
   * @param parser The `CsvParser` which can parse a CSV line into an instance of `T`
   */
  def parse[T](csv: String, hasHeader: Boolean = false)(implicit parser: CsvParser[T]): Seq[ParseResult[T]] = {
    val allLines = csv.split("\n").toList
    val usableLines = if (hasHeader) allLines.drop(1) else allLines
    usableLines.map { line =>
      val cells = line.split(",").toList
      parser(cells)
    }
  }

  def parseSkipHeader[T](csv: String)(implicit parser: CsvParser[T]): Seq[ParseResult[T]] = parse(csv, true)

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

  /**
   * Create a `CsvParser` that consumes one cell and attempts to produce a primitive of type `P`
   * @param parse The function to parse a `Cell` into `P`
   * @param typ The typeclass used to find type information for `P`
   */
  def primitiveParser[P](parse: Cell => P)(implicit typ: Typeable[P]): CsvParser[P] = {
    headInstance(s => Try(parse(s)).toOption)(s => s"Cannot parse [$s] to ${typ.describe}")
  }
  
  /**
   * Create a `CsvParser` that consumes a single cell and parses it into an instance of `T`
   * @param p The function that attempts to parse a cell into an instance of `T`
   * @param failure The function used to create an error method. The input to the method
   * will be the cell(s) that could not be parsed.
   */
  def headInstance[T](p: Cell => Option[T])(failure: String => String): CsvParser[T] = new CsvParser[T] {
    def parse(cells: Seq[Cell]): ParseResult[T] = {
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
  
  /**
   * Create a `CsvParser` that consumes `s` number of cells and attempts to produce an instance of `T`
   */
  def instance[T](s: Int)(p: Seq[Cell] => ParseResult[T]): CsvParser[T] = new CsvParser[T] {
    def parse(cells: Seq[Cell]): ParseResult[T] = {
      if (cells.length == s) {
        p(cells)
      } else {
        Left(s"Input size [${cells.length}] does not match parser expected size [$s]")
      }
    }
    def size = s
  }

  /*
   * Recursively create a parser for an HList.
   * 
   * Base Case: given an empty sequence of cells, produce an HNil
   */
  implicit val hnilParser: CsvParser[HNil] = instance(0)(s => Right((HNil)))

  /*
   * Recursively create a parser for an HList.
   * 
   * Inductive Step: given a sequence of cells, parse the front cells into `Head` and the sequence of remaining cells into `Tail`.
   * `Head` is not limited to one cell; `Head` may be a class requiring multiple cells.
   *
   */
  implicit def hconsParser[Head, Tail <: HList](implicit hParser: Lazy[CsvParser[Head]], tParser: Lazy[CsvParser[Tail]]): CsvParser[Head :: Tail] = {
    instance(hParser.value.size + tParser.value.size) { cells =>
      val (headCells, tailCells) = cells.splitAt(hParser.value.size)
      for {
        head <- hParser.value.parse(headCells).right
        tail <- tParser.value.parse(tailCells).right
      } yield head :: tail
    }
  }

  /*
   * Create a `CsvParser` for a case class of type `Case`. The parser requires that we can safely convert an HList `Repr` into a `Case`
   * @param gen The Generic allowing to convert from a `Repr` HList to a `Case` case class
   * @param reprParser The `CsvParser` that can parse a CSV line into an HList of type `Repr`
   */
  implicit def caseClassParser[Case, Repr <: HList](implicit gen: Generic.Aux[Case, Repr], reprParser: Lazy[CsvParser[Repr]]): CsvParser[Case] = {
    instance(reprParser.value.size) { cells =>
      reprParser.value.parse(cells).right.map { parsed =>
        (gen.from(parsed))
      }
    }
  }

}