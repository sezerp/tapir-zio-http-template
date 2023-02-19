package com.pawelzabczynski.infrastructure

import zio.{Scope, Task, ZIO, ZLayer, durationInt}
import doobie.hikari.HikariTransactor
import cats.effect.kernel.Resource
import doobie.util.ExecutionContexts
import doobie.Transactor
import Doobie._
import com.pawelzabczynski.util.ErrorOps
import zio.interop.catz._

object Db {

  type Env = DbConfig with Scope

  def transactorResources(config: DbConfig): Resource[Task, HikariTransactor[Task]] = {
    for {
      ec <- ExecutionContexts.fixedThreadPool[Task](config.connectThreadPoolSize)
      xa <- HikariTransactor.newHikariTransactor[Task](
        config.driver,
        config.url,
        config.username,
        config.password.value,
        ec
      )
    } yield xa
  }

  def transactorScoped: ZIO[DbConfig with Scope, Throwable, HikariTransactor[Task]] = for {
    config <- ZIO.service[DbConfig]
    xa     <- transactorResources(config).toScopedZIO
  } yield xa

  def checkConnection(xa: Transactor[Task]): Task[Unit] = {
    sql"select 1"
      .query[Int]
      .unique
      .transact(xa)
      .unit
      .catchSome { case e: Exception =>
        ZIO.logWarningCause("Database not available, waiting 5 seconds to retry...", e.toCause) *> ZIO.sleep(
          5.second
        ) *> checkConnection(xa)
      }
  }

  val live: ZLayer[Env, Throwable, HikariTransactor[Task]] = ZLayer.fromZIO(transactorScoped)
}
