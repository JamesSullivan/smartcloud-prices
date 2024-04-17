package prices

import cats.effect._
import cats.implicits.toSemigroupKOps
import com.comcast.ip4s.{ Ipv4Address, Port }
import fs2.Stream

import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

import prices.config.Config
import prices.routes.InstanceKindRoutes
import prices.routes.PriceRoutes
import prices.HttpClient
import prices.services._

object Server {



  def serve(config: Config): Stream[IO, ExitCode] = {

    val httpClient: HttpClient[IO] = new HttpClient[IO](config.smartcloud)

    val instanceKindService = SmartcloudInstanceKindService.make[IO](
      SmartcloudInstanceKindService.Config(
        config.smartcloud.baseUri,
        config.smartcloud.token
      ),
      httpClient
    )


    val priceService  = SmartcloudPriceService.make[IO](
      config.smartcloud,
      httpClient
    )

    val httpApp = (
      InstanceKindRoutes[IO](instanceKindService).routes <+> PriceRoutes[IO](priceService).routes
    ).orNotFound

    Stream
      .eval(
        EmberServerBuilder
          .default[IO]
          .withHost(Ipv4Address.fromString(config.app.host).get)
          .withPort(Port.fromInt(config.app.port).get)
          .withHttpApp(Logger.httpApp(true, true)(httpApp))
          .build
          .useForever
      )
  }

}
