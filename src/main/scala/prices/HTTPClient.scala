package prices

import org.http4s.client
import cats.effect.kernel.{ Async, Resource }
import org.http4s.client.middleware.{ Retry, RetryPolicy }
import org.http4s.ember.client.EmberClientBuilder
import prices.config.Config.SmartcloudConfig

class HttpClient[F[_]: Async](config: SmartcloudConfig) {
  val resource: Resource[F, client.Client[F]] = EmberClientBuilder.default[F].build.map { client =>
    val policy = RetryPolicy.apply[F](_ => Some(config.retryAfter))
    Retry.apply[F](policy)(client)
  }
}
