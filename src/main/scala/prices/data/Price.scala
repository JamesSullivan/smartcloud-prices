package prices.data

import io.circe.{ Decoder, HCursor }

final case class Price(kind: InstanceKind, amount: BigDecimal)


// Note we could have the priceEncoder with the object as well but split up to keep
// consistency between PriceResponse and the given InstanceKindResponse code.
object Price {
  implicit val priceDecoder: Decoder[Price] = (c: HCursor) =>
    for {
      kind <- c.downField("kind").as[String]
      amount <- c.downField("price").as[BigDecimal]
    } yield Price(InstanceKind(kind), amount)
}
