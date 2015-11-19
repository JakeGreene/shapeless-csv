package ca.jakegreene.csv

import scala.util.Try
import shapeless._

trait Parser[T] {
  def apply(s: String): Option[T]
}

/**
 * Original code is based on Travis Brown's work.
 */
object Parser {

  def apply[T](s: String)(implicit parser: Parser[T]): Option[T] = parse(s)
  def parse[T](s: String)(implicit parser: Parser[T]): Option[T] = parser(s)

  def instance[T](p: String => Option[T]): Parser[T] = new Parser[T] {
    def apply(s: String): Option[T] = p(s)
  }

  implicit val stringParser = instance(Some(_))
  implicit val intParser = instance(s => Try(s.toInt).toOption)
  implicit val doubleParser = instance(s => Try(s.toDouble).toOption)
  implicit val hnilParser: Parser[HNil] = instance(s => if (s.isEmpty) Some(HNil) else None)

  // CSV regex
  private[this] val NextCell = "^([^,]+)(?:,(.+))?$".r

  implicit def hconsParser[Head: Parser, Tail <: HList : Parser]: Parser[Head :: Tail] = {
    instance {
      case NextCell(cell, rest) => for {
        head <- parse[Head](cell)
        tail <- parse[Tail](Option(rest).getOrElse(""))
      } yield head :: tail
      case _ => None
    }
  }

  /**
   * A parser for a case class that has a parser for it's HList representation
   */
  implicit def caseClassParser[Case, Repr <: HList](implicit gen: Generic.Aux[Case, Repr], reprParser: Parser[Repr]): Parser[Case] = {
    instance(s => reprParser(s).map(gen.from))
  }

}