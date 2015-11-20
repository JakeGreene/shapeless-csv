package ca.jakegreene.csv

import scala.util.Try
import shapeless._

trait Parser[T] {
  def apply(cells: Seq[String]): Parser.ParseResult[T]
}

/**
 * Original code is based on Travis Brown's work.
 */
object Parser {
  
  type CellResult[T] = (T, Seq[String])
  type ParseResult[T] = Either[String, CellResult[T]]

  def parse[T](s: String)(implicit parser: Parser[T]): Either[String, T] = {
    val cells = s.split(",").toSeq
    parser(cells) match {
      case Right((parsed, rest)) if rest.isEmpty => Right(parsed)
      case Right((_, rest)) => Left(s"Attempted to parse but left with ${rest.mkString(",")}")
      case Left(message) => Left(message)
    }
  }

  def instance[T](p: Seq[String] => ParseResult[T]): Parser[T] = new Parser[T] {
    def apply(cells: Seq[String]): ParseResult[T] = p(cells)
  }
  
  def headInstance[T](p: String => Option[T])(failure: String => String): Parser[T] = new Parser[T] {
    def apply(cells: Seq[String]): ParseResult[T] = {
      val maybeResult = for {
        head <- cells.headOption
        parsed <- p(head)
      } yield (parsed, cells.tail)
      maybeResult.toRight(failure(cells.headOption.getOrElse("")))
    }
  }

  implicit val stringParser = headInstance(Some(_))(s => s"Cannot parse [$s] to String")
  implicit val intParser = headInstance(s => Try(s.toInt).toOption)(s => s"Cannot parse [$s] to Int")
  implicit val doubleParser = headInstance(s => Try(s.toDouble).toOption)(s => s"Cannot parse [$s] to Double")
  /*
   *  Currently accepts anything. This is due to the situation where an inner case class is not
   *  the last param of an outer case class.
   */
  implicit val hnilParser: Parser[HNil] = instance(s => Right((HNil, s)))

  // CSV regex
  private[this] val NextCell = "^([^,]+)(?:,(.+))?$".r

  implicit def hconsParser[Head, Tail <: HList](implicit hp: Lazy[Parser[Head]], tp: Lazy[Parser[Tail]]): Parser[Head :: Tail] = {
    instance { cells =>
      // Compiler bug SI-7222 prevents this from being a for-comprehension
      hp.value(cells).right.flatMap { case (head, rest) =>
        tp.value(rest).right.map { case (tail, remaining) =>
          (head :: tail, remaining)
        }
      }
    }
  }

  /**
   * A parser for a case class that has a parser for it's HList representation
   */
  implicit def caseClassParser[Case, Repr <: HList](implicit gen: Generic.Aux[Case, Repr], reprParser: Lazy[Parser[Repr]]): Parser[Case] = {
    instance { cells => 
      reprParser.value(cells).right.map { case (parsed, rest) =>
        (gen.from(parsed), rest)
      }
    }
  }

}