package com.pawelzabczynski.user

import com.pawelzabczynski.test.{CreateUserResult, TestBase, TestSupport}
import org.scalatest.concurrent.Eventually
import sttp.model.StatusCode
import com.pawelzabczynski.infrastructure.JsonSupport._

class UserApiTest extends TestBase with TestSupport with Eventually {

  "[POST] register endpoint" should "return apiKey" in {
    val request  = UserRegisterRequest("account name", "user_login", "test@email.com", "some_password_123")
    val response = requests.userRegister(request)

    response.code shouldBe StatusCode.Ok
    response.body.shouldDeserializeTo[UserRegisterResponse]
  }

  "[POST] login endpoint" should "successfully process form and return api key" in {
    val CreateUserResult(_, login, password, _) = requests.createUser()
    val request                                 = UserLoginRequest(login, password)
    val response                                = requests.userLogin(request)

    response.code shouldBe StatusCode.Ok
    response.body.shouldDeserializeTo[UserLoginResponse]
  }
}
