package com.pawelzabczynski.user

import com.pawelzabczynski.http.Http
import com.pawelzabczynski.infrastructure.JsonSupport._
import com.pawelzabczynski.util.{HttpEndpoints, IdGenerator}
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{Task, ZIO, ZLayer}

import java.util.UUID
import com.pawelzabczynski.metrics.Metrics
import com.pawelzabczynski.user.UserService.UserServiceError

class UserApi(userService: UserService, http: Http, xa: Transactor[Task]) {

  import http._

  val registerEndpoint: ZServerEndpoint[Any, Any] = baseEndpoint.post
    .in("register")
    .in(jsonBody[UserRegisterRequest])
    .out(jsonBody[UserRegisterResponse])
    .serverLogic { data =>
      (for {
        result <- userService
          .register(data)
          .mapError(UserServiceError.toThrowable)
        _ <- ZIO.succeed(Metrics.UserMetrics.registeredUsers.inc())
      } yield UserRegisterResponse(result.apiKey.id)).toTaskEither
    }

  val endpoints: HttpEndpoints = List(registerEndpoint).map(_.tag("user"))
}

object UserApi {
  type Env = UserService with Http with HikariTransactor[Task] with IdGenerator
  def create(userService: UserService, http: Http, xa: HikariTransactor[Task]): UserApi = {
    new UserApi(userService, http, xa)
  }

  val live: ZLayer[Env, Nothing, UserApi] = ZLayer.fromFunction(create _)
}

case class UserIn(name: String) extends AnyVal
case class UserOut(message: String)
case class UserRegisterRequest(accountName: String, login: String, email: String, password: String)
case class UserRegisterResponse(apiKey: UUID)
