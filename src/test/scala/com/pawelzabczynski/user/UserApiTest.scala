package com.pawelzabczynski.user

import com.pawelzabczynski.test.{TestBase, TestSupport}
import org.scalatest.concurrent.Eventually
import sttp.model.StatusCode
import com.pawelzabczynski.infrastructure.JsonSupport._

class UserApiTest extends TestBase with Eventually with TestSupport {

  "[GET] register endpoint" should "return apiKey" in {
    val request  = UserRegisterRequest("account name", "user_login", "test@email.com", "some_password_123")
    val response = requests.userRegister(request)

    response.code shouldBe StatusCode.Ok
    response.body.shouldDeserializeTo[UserRegisterResponse]
  }
}
