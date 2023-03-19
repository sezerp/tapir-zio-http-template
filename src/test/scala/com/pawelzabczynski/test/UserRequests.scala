package com.pawelzabczynski.test

import com.pawelzabczynski.user.UserRegisterRequest
import com.pawelzabczynski.infrastructure.JsonSupport._
import io.circe.syntax.EncoderOps
import sttp.client3.Response
import sttp.client3.{UriContext, basicRequest}

trait UserRequests { self: TestRequestSupport with TestSupport =>

  def userRegister(entity: UserRegisterRequest): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/register")
      .body(entity.asJson.noSpaces)
      .send(backend)
      .runUnsafe()
  }
}
