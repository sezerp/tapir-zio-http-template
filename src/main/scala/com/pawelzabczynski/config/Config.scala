package com.pawelzabczynski.config

import com.pawelzabczynski.http.HttpConfig
import com.pawelzabczynski.infrastructure.DbConfig
import com.typesafe.scalalogging.LazyLogging
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import zio.{RIO, Task, UIO, ZIO, ZLayer}

case class Config(api: HttpConfig, db: DbConfig)

object Config extends LazyLogging {
  private val load: Task[Config] = ZIO.attempt(ConfigSource.default.loadOrThrow[Config])

  val live: ZLayer[Any, Throwable, Config] = ZLayer.fromZIO(load)

  def print(config: Config): UIO[Unit] = {
    ZIO.logInfo(s"""
         |  ___________ ____    _______                   _       _
         | |___  /_   _/ __ \\  |__   __|                 | |     | |
         |    / /  | || |  | |    | | ___ _ __ ___  _ __ | | __ _| |_ ___
         |   / /   | || |  | |    | |/ _ \\ '_ ` _ \\| '_ \\| |/ _` | __/ _ \\
         |  / /__ _| || |__| |    | |  __/ | | | | | |_) | | (_| | ||  __/
         | /_____|_____\\____/     |_|\\___|_| |_| |_| .__/|_|\\__,_|\\__\\___|
         |                                         | |
         |                                         |_|
         | Configuration:
         | ---------------------
         | API: ${config.api}
         | DB: ${config.db}
         |""".stripMargin)
  }

  def print: RIO[Config, Unit] = {
    ZIO.service[Config].flatMap(c => print(c))
  }

}
