package prices.services

import prices.data.{ InstanceKind, Price }

import scala.util.control.NoStackTrace

trait PriceService[F[_]] {
  def get(instanceKind: InstanceKind): F[Either[PriceService.Exception, Price]]
}

object PriceService {

  sealed trait Exception extends NoStackTrace
  object Exception {
    case class APICallFailure(message: String) extends Exception
  }

}
