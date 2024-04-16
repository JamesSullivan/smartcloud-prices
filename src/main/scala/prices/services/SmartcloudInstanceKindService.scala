package prices.services

import cats.implicits.{ catsSyntaxApplicativeId, toFunctorOps }
import cats.effect._

import org.http4s._
import org.http4s.circe._
import org.http4s.headers.{ Accept, Authorization }
import org.http4s.Status.Successful

import prices.data._
import prices.HttpClient

object SmartcloudInstanceKindService {

  final case class Config(
      baseUri: String,
      token: String
  )

  def make[F[_]: Concurrent](config: Config, httpClient: HttpClient[F]): InstanceKindService[F] = new SmartcloudInstanceKindService(config, httpClient)

  private final class SmartcloudInstanceKindService[F[_]: Concurrent](
      config: Config,
      httpClient: HttpClient[F]
  ) extends InstanceKindService[F] {

    implicit val instanceKindsEntityDecoder: EntityDecoder[F, List[String]] = jsonOf[F, List[String]]

    val getAllUri = s"${config.baseUri}/instances"

    private val request: Request[F] =
      Request[F](
        uri = Uri.unsafeFromString(getAllUri),
        headers = Headers(Accept(MediaType.text.strings), Authorization(Credentials.Token(AuthScheme.Bearer, config.token)))
      )

    override def getAll(): F[Either[InstanceKindService.Exception, List[InstanceKind]]] =
      httpClient.resource.use { client =>
        client.run(request).use {
          case Successful(response) =>
            response.as[List[String]].map((payload: List[String]) => Right(payload.map(InstanceKind)))
          case response =>
            val result: Either[InstanceKindService.Exception, List[InstanceKind]] =
              Left(InstanceKindService.Exception.APICallFailure(response.status.toString()))
            result.pure
        }
      }
  }

}
