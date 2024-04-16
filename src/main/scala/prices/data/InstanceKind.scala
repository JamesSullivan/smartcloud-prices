package prices.data

final case class InstanceKind(getString: String) extends AnyVal

/** P.24 we can wrap a single field and extend the AnyVal abstract class to avoid some runtime costs
  *
  * Do we want to use smart constructors or sealed abstract classes or opague types (Scala 3) or even Refinement types
  */
