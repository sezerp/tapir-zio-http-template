package com.pawelzabczynski.http

import com.pawelzabczynski.Fail
import com.pawelzabczynski.infrastructure.JsonSupport._
import Fail.{IncorrectInput, Unauthorized}
import sttp.tapir.{EndpointOutput, PublicEndpoint, Tapir}
import sttp.tapir.json.circe.TapirJsonCirce
import io.circe.Printer
import sttp.model.StatusCode
import sttp.tapir.generic.auto._
import zio.{Task, ZIO, ZLayer}

import java.util.UUID

class Http() extends Tapir with TapirJsonCirce {
  val jsonErrorOutOutput: EndpointOutput[ErrorOut]                          = jsonBody[ErrorOut]
  val failOutput: EndpointOutput[(StatusCode, ErrorOut)]                    = statusCode.and(jsonErrorOutOutput)
  val baseEndpoint: PublicEndpoint[Unit, (StatusCode, ErrorOut), Unit, Any] = endpoint.errorOut(failOutput)
  val secureEndpoint: PublicEndpoint[UUID, (StatusCode, ErrorOut), Unit, Any] =
    baseEndpoint.in(auth.bearer[String]().map(UUID.fromString _)(_.toString))

  private val failToResponseData: Fail => (StatusCode, String) = {
    case IncorrectInput(msg) => (StatusCode.BadRequest, msg)
    case Unauthorized        => (StatusCode.Unauthorized, "Unauthorized")
    case _                   => (StatusCode.InternalServerError, "Internal server error")
  }

  override def jsonPrinter: Printer = noNullsPrinter

  implicit class TaskOut[T](f: Task[T]) {
    def toTaskEither: Task[Either[(StatusCode, ErrorOut), T]] = {
      f.map(t => Right(t)).catchSome { case f: Fail =>
        val (statusCode, errorMsg) = failToResponseData(f)
        ZIO.succeed(Left((statusCode, ErrorOut(errorMsg))))
      }
    }
  }
}

object Http {

  def create(): Http = {
    new Http()
  }
  val live: ZLayer[Any, Nothing, Http] = ZLayer.fromFunction(Http.create _)
}
