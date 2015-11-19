package ca.jakegreene.csv

import shapeless._

case class Person(name: String, age: Int)

object Main extends App {
  val maybeJake = Parser[Person]("Jake Greene,26")
  println(maybeJake)
  val maybeNum = Parser[Int]("300")
  println(maybeNum)
  val maybeNot = Parser[Person]("Jake,Greene")
  println(maybeNot)
}