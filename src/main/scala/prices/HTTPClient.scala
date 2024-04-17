package prices

import scala.concurrent.duration._
import org.http4s.client
import cats.effect.kernel.{ Async, Resource }
import org.http4s.client.middleware.{ Retry, RetryPolicy }
import org.http4s.ember.client.EmberClientBuilder
import prices.config.Config.SmartcloudConfig

class HttpClient[F[_]: Async](config: SmartcloudConfig) {

  val retryPolicy: RetryPolicy[F] = RetryPolicy[F] { (attempts: Int) =>
    if (attempts >= 5) None
    else Some(500.milliseconds)
  }

  val resource: Resource[F, client.Client[F]] = EmberClientBuilder.default[F].build.map { client =>
    Retry.apply[F](retryPolicy)(client)
  }
}
