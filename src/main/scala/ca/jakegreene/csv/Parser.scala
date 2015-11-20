package ca.jakegreene.csv

import scala.util.Try
import shapeless._

trait Parser[T] {
  def apply(cells: Seq[String]): Option[Parser.CellResult[T]]
}

/**
 * Original code is based on Travis Brown's work.
 */
object Parser {
  
  type CellResult[T] = (T, Seq[String])

  //def apply[T](s: String)(implicit parser: Parser[T]): Option[T] = parse(s)
  def parse[T](s: String)(implicit parser: Parser[T]): Option[T] = {
    val cells = s.split(",").toSeq
    for {
      (result, left) <- parser(cells)
      if left.isEmpty
    } yield result
  }

  def instance[T](p: Seq[String] => Option[CellResult[T]]): Parser[T] = new Parser[T] {
    def apply(cells: Seq[String]): Option[CellResult[T]] = p(cells)
  }
  
  def headInstance[T](p: String => Option[T]): Parser[T] = new Parser[T] {
    def apply(cells: Seq[String]): Option[CellResult[T]] = for {
      head <- cells.headOption
      parsed <- p(head)
    } yield (parsed, cells.tail)
  }

  implicit val stringParser = headInstance(Some(_))
  implicit val intParser = headInstance(s => Try(s.toInt).toOption)
  implicit val doubleParser = headInstance(s => Try(s.toDouble).toOption)
  implicit val hnilParser: Parser[HNil] = instance(s => if (s.isEmpty) Some((HNil, Seq())) else None)

  // CSV regex
  private[this] val NextCell = "^([^,]+)(?:,(.+))?$".r

  implicit def hconsParser[Head, Tail <: HList](implicit hp: Lazy[Parser[Head]], tp: Lazy[Parser[Tail]]): Parser[Head :: Tail] = {
    instance { cells =>
      for {
        (head, rest) <- hp.value(cells)
        (tail, leftover) <- tp.value(rest)
      } yield (head :: tail, leftover)
    }
  }

  /**
   * A parser for a case class that has a parser for it's HList representation
   */
  implicit def caseClassParser[Case, Repr <: HList](implicit gen: Generic.Aux[Case, Repr], reprParser: Lazy[Parser[Repr]]): Parser[Case] = {
    instance { s => 
      reprParser.value(s).map { case (parsed, rest) =>
        (gen.from(parsed), rest)
      }
    }
  }

}