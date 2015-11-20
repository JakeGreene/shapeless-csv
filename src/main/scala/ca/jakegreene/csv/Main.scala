package ca.jakegreene.csv

import shapeless._

case class Person(name: String, age: Int)
case class Nested(name: String, next: Nested)

object Main extends App {
}