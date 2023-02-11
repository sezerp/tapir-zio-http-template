package com.pawelzabczynski

import com.pawelzabczynski.http.{Http, HttpApi, HttpConfig}
import com.pawelzabczynski.infrastructure.ZIOLogger
import com.pawelzabczynski.user.UserApi
import com.typesafe.scalalogging.StrictLogging
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}
import zio.interop.catz._

object Application extends ZIOAppDefault with StrictLogging {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    zio.Runtime.removeDefaultLoggers >>> zio.Runtime.addLogger(ZIOLogger.make)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

    ZIO.executor.flatMap { executor =>
      val http    = new Http()
      val userApi = new UserApi(http)
      val httpApi = new HttpApi(http, userApi.endpoints, HttpConfig("0.0.0.0", 8080))
      httpApi.resources(executor.asExecutionContext).use(_ => ZIO.never)
    }
  }
}
