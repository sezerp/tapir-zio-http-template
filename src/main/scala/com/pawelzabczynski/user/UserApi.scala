package com.pawelzabczynski.user

import com.pawelzabczynski.http.Http
import com.pawelzabczynski.infrastructure.JsonSupport._
import com.pawelzabczynski.util.{HttpEndpoints, Id, IdGenerator}
import doobie.hikari.HikariTransactor
import doobie.util.transactor.Transactor
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{Task, ZIO, ZLayer}
import com.softwaremill.tagging._
import com.pawelzabczynski.metrics.Metrics
import com.pawelzabczynski.security.apiKey.ApiKey
import com.pawelzabczynski.security.apiKey.ApiKey.ApiKeyId
import com.pawelzabczynski.security.auth.Auth
import com.pawelzabczynski.security.auth.AuthService.AuthError
import com.pawelzabczynski.user.UserService.UserServiceError

class UserApi(userService: UserService, auth: Auth[ApiKey], http: Http, xa: Transactor[Task]) {

  import http._

  val registerEndpoint: ZServerEndpoint[Any, Any] = baseEndpoint.post
    .in("register")
    .in(jsonBody[UserRegisterRequest])
    .out(jsonBody[UserRegisterResponse])
    .serverLogic { request =>
      (for {
        result <- userService
          .register(request)
          .mapError(UserServiceError.toThrowable)
        _ <- ZIO.succeed(Metrics.UserMetrics.registeredUsers.inc())
      } yield UserRegisterResponse(result.apiKey.id)).toTaskEither
    }

  val loginEndpoint: ZServerEndpoint[Any, Any] = baseEndpoint.post
    .in("login")
    .in(jsonBody[UserLoginRequest])
    .out(jsonBody[UserLoginResponse])
    .serverLogic { request =>
      userService
        .login(request)
        .mapError(UserServiceError.toThrowable)
        .map(apiKey => UserLoginResponse(apiKey.id))
        .toTaskEither
    }

  val changePassword: ZServerEndpoint[Any, Any] = secureEndpoint
    .serverSecurityLogic(token => auth.auth(token).mapError(AuthError.toThrowable).toTaskEither)
    .post
    .in("changepassword")
    .in(jsonBody[UserChangePasswordRequest])
    .out(jsonBody[UserChangePasswordResponse])
    .serverLogic(user =>
      data =>
        userService
          .changePassword(user, data)
          .mapError(UserServiceError.toThrowable)
          .as(UserChangePasswordResponse())
          .toTaskEither
    )

  val endpoints: HttpEndpoints = List(registerEndpoint, loginEndpoint).map(_.tag("user"))
}

object UserApi {
  type Env = UserService with Auth[ApiKey] with Http with HikariTransactor[Task] with IdGenerator
  def create(userService: UserService, auth: Auth[ApiKey], http: Http, xa: HikariTransactor[Task]): UserApi = {
    new UserApi(userService, auth, http, xa)
  }

  val live: ZLayer[Env, Nothing, UserApi] = ZLayer.fromFunction(create _)
}

case class UserRegisterRequest(accountName: String, login: String, email: String, password: String)
case class UserRegisterResponse(apiKey: Id @@ ApiKey)

case class UserLoginRequest(loginOrEmail: String, password: String)
case class UserLoginResponse(apiKey: ApiKeyId)

case class UserChangePasswordRequest(currentPassword: String, newPassword: String)
case class UserChangePasswordResponse()
