package com.pawelzabczynski

import com.pawelzabczynski.config.Config
import com.pawelzabczynski.http.{Http, HttpApi, HttpConfig}
import com.pawelzabczynski.infrastructure.{Db, DbConfig, DbTransactor, ZIOLogger}
import com.pawelzabczynski.user.UserApi
import com.typesafe.scalalogging.StrictLogging
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object Application extends ZIOAppDefault with StrictLogging {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] =
    zio.Runtime.removeDefaultLoggers >>> zio.Runtime.addLogger(ZIOLogger.make)

  override def run: ZIO[Any with ZIOAppArgs with Scope, Any, Any] = {
    Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))
    val program = ZIO.executor.flatMap { executor =>
      (for {
        http    <- ZIO.service[Http]
        userApi <- ZIO.service[UserApi]
        _       <- Config.print
        db      <- ZIO.service[Db]
        _       <- db.checkAndMigrate()
        httpApi = new HttpApi(http, userApi.endpoints, HttpConfig("0.0.0.0", 8080))
        _ <- httpApi.scoped(executor.asExecutionContext)
      } yield ()) *> ZIO.never
    }

    program
      .provide(
        Config.live,
        DbConfig.live,
        Http.live,
        DbTransactor.live,
        Db.live,
        UserApi.live,
        Scope.default
      )
  }
}
