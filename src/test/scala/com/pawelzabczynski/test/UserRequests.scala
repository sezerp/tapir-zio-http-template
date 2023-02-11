package com.pawelzabczynski.test

import sttp.client3.Response
import sttp.client3.{UriContext, basicRequest}

trait UserRequests { self: TestRequestSupport with TestSupport =>

  def helloGetRequest(name: String): Response[Either[String, String]] = {
    basicRequest
      .get(uri"$basePath/hello?name=$name")
      .send(backend)
      .unwrap
  }
}
