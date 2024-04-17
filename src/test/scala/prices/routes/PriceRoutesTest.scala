import cats.effect.IO
import cats.effect.unsafe.implicits.global

import munit.FunSuite

import org.http4s.headers.Authorization
import org.http4s.{ AuthScheme, Credentials, Method, Request, Status, Uri }

import prices.data.{ InstanceKind, Price }
import prices.routes.PriceRoutes
import prices.services.PriceService
import prices.services.PriceService.Exception.APICallFailure


// Mock Tests that do not actually use smartcloud
class PriceRoutesTest extends FunSuite {

  test("PriceRoutes Failure Case - 401 Unauthorized") {
    val priceService = new PriceService[IO]() {
      override def get(instanceKind: InstanceKind): IO[Either[PriceService.Exception, Price]] = 
        IO(Left(APICallFailure("401 Unauthorized")))
    }
    val priceRoutes = PriceRoutes(priceService)
    val (status, message) = (for {
      response <- priceRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(s"${priceRoutes.prefix}?kind=sc2-micro"))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Unauthorized attempt")))
              )
      message <- response.as[String]
    } yield (response.status, message)).unsafeRunSync()

    assertEquals(status, Status.InternalServerError)
    assertEquals(message, """401 Unauthorized""")
  }

  test("PriceRoutes Failure Case - 404 Not Found") {
    val priceService = new PriceService[IO]() {
      override def get(instanceKind: InstanceKind): IO[Either[PriceService.Exception, Price]] = 
        IO(Left(APICallFailure("404 Not Found")))
    }
    val priceRoutes = PriceRoutes(priceService)
    val (status, message) = (for {
      response <- priceRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(s"${priceRoutes.prefix}?kind=sc2-micro"))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Unauthorized attempt")))
              )
      message <- response.as[String]
    } yield (response.status, message)).unsafeRunSync()

    assertEquals(status, Status.InternalServerError)
    assertEquals(message, """404 Not Found""")
  }

    test("PriceRoutes Success Case") {
    val priceService = new PriceService[IO]() {
      override def get(instanceKind: InstanceKind): IO[Either[PriceService.Exception, Price]] = 
        IO(Right(Price(InstanceKind("sc2-micro"), 0.332)))
    }
    val priceRoutes = PriceRoutes(priceService)
    val (status, message) = (for {
      response <- priceRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(s"${priceRoutes.prefix}?kind=sc2-micro"))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Pretend Authorized")))
              )
      message <- response.as[String]
    } yield (response.status, message)).unsafeRunSync()

    assertEquals(status, Status.Ok)
    assertEquals(message, """{"kind":"sc2-micro","amount":0.332}""")
  }
}
