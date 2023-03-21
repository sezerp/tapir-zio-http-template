package com.pawelzabczynski.test

import com.pawelzabczynski.user.{UserLoginRequest, UserRegisterRequest, UserRegisterResponse}
import com.pawelzabczynski.infrastructure.JsonSupport._
import com.pawelzabczynski.security.apiKey.ApiKey.ApiKeyId
import io.circe.syntax.EncoderOps
import sttp.client3.Response
import sttp.client3.{UriContext, basicRequest}

import java.util.UUID

trait UserRequests { self: TestRequestSupport with TestSupport =>

  def createUser(): CreateUserResult = {
    val login       = s"user_${UUID.randomUUID()}"
    val email       = s"user-$login@email.com"
    val password    = UUID.randomUUID().toString
    val accountName = s"account-${UUID.randomUUID()}"
    val entity      = UserRegisterRequest(accountName, login, email, password)
    val response    = userRegister(entity).body.shouldDeserializeTo[UserRegisterResponse]

    CreateUserResult(email, login, password, response.apiKey)
  }

  def userRegister(entity: UserRegisterRequest): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/register")
      .body(entity.asJson.noSpaces)
      .send(backend)
      .runUnsafe()
  }

  def userLogin(entity: UserLoginRequest): Response[Either[String, String]] = {
    basicRequest
      .post(uri"$basePath/login")
      .body(entity.asJson.noSpaces)
      .send(backend)
      .runUnsafe()
  }
}

final case class CreateUserResult(email: String, login: String, password: String, apiKey: ApiKeyId)
