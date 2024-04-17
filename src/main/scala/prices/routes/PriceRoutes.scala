package prices.routes

import cats.effect._
import cats.implicits._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.{ EntityEncoder, HttpRoutes }
import prices.data.InstanceKind
import prices.routes.protocol._
import prices.services.PriceService.Exception.APICallFailure
import prices.services.PriceService

final case class PriceRoutes[F[_]: Sync](priceService: PriceService[F]) extends Http4sDsl[F] {

  val prefix = "/prices"

  implicit val priceResponseEncoder: EntityEncoder[F, PriceResponse] = jsonEncoderOf[F, PriceResponse]

  private val get: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root :? InstanceKindQueryParam(instanceKind: InstanceKind) =>
      priceService
        .get(instanceKind)
        .flatMap {
          case Left(APICallFailure(message)) => InternalServerError(message)
          case Right(price)                  => Ok(PriceResponse(price))
        }
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )

}



