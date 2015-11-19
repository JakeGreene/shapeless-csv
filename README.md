Shapeless CSV
=========================

A boilerplate-free CSV parser using shapeless.

Original idea and code from Travis Brown's http://meta.plasm.us/posts/2015/11/08/type-classes-and-generic-derivation/

Usage
-----

Able to parse CSV strings into "flat" case classes without the need for boilerplate.

```
scala> import ca.jakegreene.csv._
import ca.jakegreene.csv._

scala> case class Person(name: String, age: Int)
defined class Person

scala> val jake = Parser[Person]("Jake Greene,26")
jake: Option[Person] = Some(Person(Jake Greene,26))

scala> val runtimeTypeCheck = Parser[Person]("Jake Greene,apple")
runtimeTypeCheck: Option[Person] = None

scala> val compiletimeTypeCheck = Parser[List[Person]]("Jake Greene,26")
<console>:15: error: could not find implicit value for parameter parser: ca.jakegreene.csv.Parser[List[Person]]
       val compiletimeTypeCheck = Parser[List[Person]]("Jake Greene,26")
```

Future Development
------------------

* CSV can be parsed into nested case classes
```
scala> case class Address(number: Int, street: String)
defined class Address

scala> case class Person(name: String, age: Int, home: Address)
defined class Person

// Current Behaviour
scala> val pm = Parser[Person]("Sir John A. Macdonald,42,24,Sussex Drive")
pm: Option[Person] = None

// Ideal Behaviour
scala> val pm = Parser[Person]("Sir John A. Macdonald,42,24,Sussex Drive")
pm: Option[Person] = Some(Person(Sir John A. Macdonald,42,Address(24,Sussex Drive)))
```
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
