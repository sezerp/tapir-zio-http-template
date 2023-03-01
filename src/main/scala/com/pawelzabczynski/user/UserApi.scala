package com.pawelzabczynski.user

import com.pawelzabczynski.http.Http
import com.pawelzabczynski.infrastructure.JsonSupport._
import com.pawelzabczynski.util.HttpEndpoints
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{Task, ZIO, ZLayer}

import java.time.Instant
import java.util.UUID
import com.pawelzabczynski.infrastructure.Doobie._
import com.pawelzabczynski.metrics.Metrics
import zio.interop.catz._

class UserApi(http: Http, xa: Transactor[Task]) {

  import http._

  val helloEndpoint: ZServerEndpoint[Any, Any] = baseEndpoint.get
    .in("hello")
    .in(query[UserIn]("name"))
    .out(jsonBody[UserOut])
    .serverLogic { user =>
      (for {
        _ <- ZIO.logInfo(s"User: $user")
        _ <- ZIO.logInfo(s"## Transactor: $xa")
        _ <- UserModel
          .insert(
            User(
              UUID.randomUUID().toString,
              UUID.randomUUID().toString,
              UUID.randomUUID().toString,
              UUID.randomUUID().toString,
              UUID.randomUUID().toString,
              Instant.now()
            )
          )
          .transact(xa)
        _ <- ZIO.succeed(Metrics.UserMetrics.registeredUsers.inc())
        _ <- ZIO.logInfo("user created.")
      } yield UserOut(s"Hello ${user.name}")).toOut
    }

  val endpoints: HttpEndpoints = List(helloEndpoint)
}

object UserApi {
  type Env = Http with HikariTransactor[Task]
  def create(http: Http, xa: HikariTransactor[Task]): UserApi = {
    new UserApi(http, xa)
  }

  val live: ZLayer[Env, Nothing, UserApi] = ZLayer.fromFunction(create _)
}

case class UserIn(name: String) extends AnyVal
case class UserOut(message: String)
