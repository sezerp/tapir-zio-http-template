package com.pawelzabczynski.user

import com.pawelzabczynski.test.{TestBase, TestSupport}
import org.scalatest.concurrent.Eventually
import sttp.model.StatusCode
import com.pawelzabczynski.infrastructure.JsonSupport._

class UserApiTest extends TestBase with Eventually with TestSupport {

  "[GET] hello endpoint" should "return hello message as a json" in {
    val name     = "test name"
    val response = requests.helloGetRequest(name)

    response.code shouldBe StatusCode.Ok
    response.body.shouldDeserializeTo[UserOut]
    response.body.shouldDeserializeTo[UserOut].message shouldBe s"Hello $name"
  }
}
