package prices.config

import cats.effect.kernel.Sync

import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.duration.FiniteDuration

case class Config(
    app: Config.AppConfig,
    smartcloud: Config.SmartcloudConfig
)

object Config {

  case class AppConfig(
      host: String,
      port: Int
  )

  case class SmartcloudConfig(
      baseUri: String,
      retryAfter: FiniteDuration,
      token: String
  )

  def load[F[_]: Sync]: F[Config] =
    Sync[F].delay(ConfigSource.default.loadOrThrow[Config])

}
