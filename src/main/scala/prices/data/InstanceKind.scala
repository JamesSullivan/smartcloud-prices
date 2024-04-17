package prices.data


/** machine instance kind wrapping a single field
  * (extends the AnyVal abstract class to avoid some runtime costs)
  * For more type safe alternatives we could have used smart constructors or sealed abstract 
  * classes or opague types (Scala 3) or even Refinement types
  */
final case class InstanceKind(getString: String) extends AnyVal
