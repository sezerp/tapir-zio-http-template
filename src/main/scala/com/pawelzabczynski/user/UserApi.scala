package com.pawelzabczynski.user

import com.pawelzabczynski.http.Http
import com.pawelzabczynski.infrastructure.JsonSupport._
import com.pawelzabczynski.util.HttpEndpoints
import sttp.tapir.generic.auto._
import sttp.tapir.ztapir.ZServerEndpoint
import zio.{ZIO, ZLayer}

class UserApi(http: Http) {

  import http._

  val helloEndpoint: ZServerEndpoint[Any, Any] = baseEndpoint.get
    .in("hello")
    .in(query[UserIn]("name"))
    .out(jsonBody[UserOut])
    .serverLogic { user =>
      ZIO.logInfo(s"User: $user") *> ZIO.succeed(UserOut(s"Hello ${user.name}")).toOut
    }

  val endpoints: HttpEndpoints = List(helloEndpoint)
}

object UserApi {
  type Env = Http
  def create(http: Http): UserApi = {
    new UserApi(http)
  }

  val live: ZLayer[Env, Nothing, UserApi] = ZLayer.fromFunction(create _)
}

case class UserIn(name: String) extends AnyVal
case class UserOut(message: String)
