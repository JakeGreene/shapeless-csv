Shapeless CSV
=========================

A boilerplate-free CSV parser using shapeless.

Original idea and code from Travis Brown's http://meta.plasm.us/posts/2015/11/08/type-classes-and-generic-derivation/

Usage
-----

Able to parse CSV strings into case classes without the need for boilerplate. Supports nested case classes.

```scala
import ca.jakegreene.csv._

case class Address(number: Int, street: String)
case class Person(name: String, age: Int, home: Address)

val csv = "Jake Greene,26,0,Madeup St.\nJG,26,10,Somewhere Road"
val jakes = CsvParser.parse[Person](csv)
/*
 * List(Right(Person(Jake Greene,26,Address(0,Madeup St.))),
 *      Right(Person(JG,26,Address(10,Somewhere Road))))
 */
println(jakes) 

val runtimeCheck = CsvParser.parse[Person]("Jake Greene,26,true,Madeup St.")
// List(Left(Cannot parse [true] to Int))
println(runtimeCheck) 

trait Invalid
/* <console>:17: error: could not find implicit value for parameter parser: ca.jakegreene.csv.CsvParser[Invalid]
 *     val compiletimeCheck = CsvParser.parse[Invalid]("Jake Greene,26,0,Madeup St.")
 */
val compiletimeCheck = CsvParser.parse[Invalid]("Jake Greene,26,0,Madeup St.")
```
