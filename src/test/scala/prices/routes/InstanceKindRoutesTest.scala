package prices.routes

import cats.effect.IO
import cats.effect.unsafe.implicits.global

import munit.FunSuite

import org.http4s.headers.Authorization
import org.http4s.{ AuthScheme, Credentials, Method, Request, Status, Uri }

import prices.data.InstanceKind
import prices.routes.InstanceKindRoutes
import prices.services.InstanceKindService
import prices.services.InstanceKindService.Exception.APICallFailure


// Mock Tests that do not actually use smartcloud
class InstanceKindRoutesTest extends FunSuite {

  test("InstanceKindRoutes Failure Case - 401 Unauthorized") {
    val instanceKindService = new InstanceKindService[IO]() {
      override def getAll(): IO[Either[InstanceKindService.Exception, List[InstanceKind]]] = 
        IO(Left(APICallFailure("401 Unauthorized")))
    }
    val instanceKindRoutes = InstanceKindRoutes(instanceKindService)
    val (status, message) = (for {
      response <- instanceKindRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(instanceKindRoutes.prefix))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Unauthorized attempt")))
              )
      message <- response.as[String]
    } yield (response.status, message)).unsafeRunSync()

    assertEquals(status, Status.InternalServerError)
    assertEquals(message, """401 Unauthorized""")
  }

  test("InstanceKindRoutes Failure Case - 404 Not found") {
    val instanceKindService = new InstanceKindService[IO]() {
      override def getAll(): IO[Either[InstanceKindService.Exception, List[InstanceKind]]] = 
        IO(Left(APICallFailure("404 Not Found")))
    }
    val instanceKindRoutes = InstanceKindRoutes(instanceKindService)
    val (status, message) = (for {
      response <- instanceKindRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString("wrong-url"))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Pretend Authorized")))
              )
      message <- response.as[String]
    } yield (response.status, message)).unsafeRunSync()

    assertEquals(status, Status.NotFound)
    assertEquals(message, """Not found""")
  }


  test("InstanceKindRoutes Success Case") {
    val instanceKindService = new InstanceKindService[IO]() {
      override def getAll(): IO[Either[InstanceKindService.Exception, List[InstanceKind]]] = 
        IO(Right(List(InstanceKind("sc2-micro"), InstanceKind("sc2-small"), InstanceKind("sc2-medium"))))
    }
    val instanceKindRoutes = InstanceKindRoutes(instanceKindService)
    val (status, message) = (for {
      response <- instanceKindRoutes.routes.orNotFound.run(
                Request(method = Method.GET, uri = Uri.unsafeFromString(instanceKindRoutes.prefix))
                  .withHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, "Pretend Authorized")))
              )
      message <- response.as[String]
    } yield (response.status, message)).unsafeRunSync()

    assertEquals(status, Status.Ok)
    assertEquals(message, """[{"kind":"sc2-micro"},{"kind":"sc2-small"},{"kind":"sc2-medium"}]""")
  }



}
