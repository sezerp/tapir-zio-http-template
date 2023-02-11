package com.pawelzabczynski.http

import cats.effect.kernel.Resource
import com.pawelzabczynski.infrastructure.CorrelationIdInterceptor
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import sttp.tapir.emptyInput
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, Http4sServerOptions}
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.model.ValuedEndpointOutput
import sttp.tapir.swagger.SwaggerUIOptions
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import zio.{Task, UIO, ZIO}
import zio.interop.catz._
import com.pawelzabczynski.util._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContext

class HttpApi(http: Http, endpoints: HttpEndpoints, config: HttpConfig) extends StrictLogging {
  private val apiContextPath = List("api", "v1")

  val serverOptions: Http4sServerOptions[Task] = Http4sServerOptions
    .customiseInterceptors[Task]
    .prependInterceptor(CorrelationIdInterceptor)
    .defaultHandlers(msg => ValuedEndpointOutput(http.jsonErrorOutOutput, ErrorOut(msg)), notFoundWhenRejected = true)
    .corsInterceptor(CORSInterceptor.default[Task])
    .serverLog {
      Http4sServerOptions
        .defaultServerLog[Task]
        .doLogWhenHandled(logDebug)
        .doLogAllDecodeFailures(logDebug)
        .doLogExceptions((msg, e) => ZIO.logErrorCause(msg, e.toCause))
        .doLogWhenReceived(msg => ZIO.logDebug(msg))
    }
    .options

  lazy val routes: HttpRoutes[Task] = Http4sServerInterpreter(serverOptions).toRoutes(allEndpoints)
  lazy val allEndpoints: List[ServerEndpoint[Any, Task]] = {
    val docsEndpoints =
      SwaggerInterpreter(swaggerUIOptions = SwaggerUIOptions.default.copy(contextPath = apiContextPath))
        .fromServerEndpoints(endpoints, "Tapir-Zio-Http", "1.0")

    val apiEndpoints = (endpoints ++ docsEndpoints).map { se =>
      se.prependSecurityIn(apiContextPath.foldLeft(emptyInput)(_ / _))
    }

    apiEndpoints
  }

  def resources(ex: ExecutionContext): Resource[Task, org.http4s.server.Server] = BlazeServerBuilder[Task]
    .withExecutionContext(ex)
    .bindHttp(config.port, config.host)
    .withHttpApp(routes.orNotFound)
    .resource

  private def logDebug(msg: String, maybeError: Option[Throwable]): UIO[Unit] = {
    maybeError.fold(ZIO.logDebug(msg))(e => ZIO.logDebugCause(msg, e.toCause))
  }
}

case class ErrorOut(error: String)
