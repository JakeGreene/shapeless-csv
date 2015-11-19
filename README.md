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
