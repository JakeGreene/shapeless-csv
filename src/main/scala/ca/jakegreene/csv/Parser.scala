package ca.jakegreene.csv

import scala.util.Try
import shapeless._

trait Parser[T] {
  def apply(cells: Seq[String]): Parser.ParseResult[T]
  def size: Int
}

object Parser {

  type ParseResult[T] = Either[String, T]

  def parse[T](s: String)(implicit parser: Parser[T]): Seq[ParseResult[T]] = {
    val lines = s.split("\n").toSeq
    lines.map { line =>
      val cells = line.split(",").toSeq
      parser(cells)
    }
  }

  def instance[T](s: Int)(p: Seq[String] => ParseResult[T]): Parser[T] = new Parser[T] {
    def apply(cells: Seq[String]): ParseResult[T] = {
      if (cells.length == s) {
        p(cells)
      } else {
        Left(s"Input size [${cells.length}] does not match parser expected size [$s]")
      }
    }
    def size = s
  }

  def headInstance[T](p: String => Option[T])(failure: String => String): Parser[T] = new Parser[T] {
    def apply(cells: Seq[String]): ParseResult[T] = {
      cells match {
        case head +: Nil =>
          val maybeParsed = p(head)
          maybeParsed.toRight(failure(head))
        case _ =>
          Left(failure(cells.mkString(", ")))
      }
    }
    def size = 1
  }

  implicit val stringParser = headInstance(Some(_))(s => s"Cannot parse [$s] to String")
  implicit val intParser = headInstance(s => Try(s.toInt).toOption)(s => s"Cannot parse [$s] to Int")
  implicit val doubleParser = headInstance(s => Try(s.toDouble).toOption)(s => s"Cannot parse [$s] to Double")
  /*
   *  Currently accepts anything. This is due to the situation where an inner case class is not
   *  the last param of an outer case class.
   */
  implicit val hnilParser: Parser[HNil] = instance(0)(s => Right((HNil)))

  // CSV regex
  private[this] val NextCell = "^([^,]+)(?:,(.+))?$".r

  implicit def hconsParser[Head, Tail <: HList](implicit hp: Lazy[Parser[Head]], tp: Lazy[Parser[Tail]]): Parser[Head :: Tail] = {
    instance(hp.value.size + tp.value.size) { cells =>
      // Compiler bug SI-7222 prevents this from being a for-comprehension
      val (headCells, tailCells) = cells.splitAt(hp.value.size)
      hp.value(headCells).right.flatMap { case (head) =>
        tp.value(tailCells).right.map { case (tail) =>
          (head :: tail)
        }
      }
    }
  }

  /**
   * A parser for a case class that has a parser for it's HList representation
   */
  implicit def caseClassParser[Case, Repr <: HList](implicit gen: Generic.Aux[Case, Repr], reprParser: Lazy[Parser[Repr]]): Parser[Case] = {
    instance(reprParser.value.size) { cells =>
      reprParser.value(cells).right.map { parsed =>
        (gen.from(parsed))
      }
    }
  }

}