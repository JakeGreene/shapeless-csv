Shapeless CSV
=========================

A boilerplate-free CSV parser using shapeless.

Original idea and code from Travis Brown's http://meta.plasm.us/posts/2015/11/08/type-classes-and-generic-derivation/

Usage
-----

Able to parse CSV strings into case classes without the need for boilerplate. Supports nested case classes.

```
scala> import ca.jakegreene.csv._
import ca.jakegreene.csv._

scala> case class Address(number: Int, street: String)
defined class Address

scala> case class Person(name: String, age: Int, home: Address)
defined class Person

scala> val jake = Parser.parse[Person]("Jake Greene,26,0,Madeup St.")
jake: Either[String,Person] = Right(Person(Jake Greene,26,Address(0,Madeup St.)))

scala> val runtimeCheck = Parser.parse[Person]("Jake Greene,26,true,Madeup St.")
runtimeCheck: Either[String,Person] = Left(Cannot parse [true] to Int)

scala> val compiletimeCheck = Parser.parse[List[Person]]("Jake Greene,26,0,Madeup St.")
<console>:17: error: could not find implicit value for parameter parser: ca.jakegreene.csv.Parser[List[Person]]
       val compiletimeCheck = Parser.parse[List[Person]]("Jake Greene,26,0,Madeup St.")
```

Future Development
------------------

* CSV can be parsed into a collection of case classes.
```
// Current Behaviour
scala> val people = Parser[List[Person]]("Jake Greene,26")
<console>:15: error: could not find implicit value for parameter parser: ca.jakegreene.csv.Parser[List[Person]]
       val compiletimeTypeCheck = Parser[List[Person]]("Jake Greene,26")

// Ideal Behaviour
scala> val people = Parser[List[Person]]("Jake Greene,26")
people: Option[List[Person]] = Some(List(Person(Jake Greene,26)))
```
