package com.pawelzabczynski

import sttp.tapir.server.ServerEndpoint
import zio.{Cause, Task}

package object util {
  type HttpEndpoints = List[ServerEndpoint[Any, Task]]

  implicit class ErrorOps[E <: Throwable](e: E) {
    implicit def toCause: Cause[E] = Cause.fail(e)
  }
}
