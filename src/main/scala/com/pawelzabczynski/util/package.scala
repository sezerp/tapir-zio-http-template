package com.pawelzabczynski

import sttp.tapir.server.ServerEndpoint
import com.softwaremill.tagging._
import zio.{Cause, Task}

import java.util.UUID

package object util {
  type Id <: UUID
  type HttpEndpoints = List[ServerEndpoint[Any, Task]]

  implicit class ErrorOps[E <: Throwable](e: E) {
    implicit def toCause: Cause[E] = Cause.fail(e)
  }

  implicit class IdOps(val s: UUID) extends AnyVal {
    def asId[T]: Id @@ T = s.asInstanceOf[Id @@ T]
  }

}
