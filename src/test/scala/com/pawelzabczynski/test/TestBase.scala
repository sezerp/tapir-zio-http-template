package com.pawelzabczynski.test

import com.pawelzabczynski.http.{Http, HttpApi}
import com.pawelzabczynski.user.UserApi
import com.typesafe.scalalogging.StrictLogging
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.SttpBackend
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.client3.testing.SttpBackendStub
import sttp.tapir.server.stub.TapirStubInterpreter
import zio.interop.catz._
import zio.Task

class TestBase extends AnyFlatSpec with Matchers with BeforeAndAfterAll with TestEmbeddedPostgres with StrictLogging {
  Thread.setDefaultUncaughtExceptionHandler((t, e) => logger.error("Uncaught exception in thread: " + t, e))

  private var httpApi: HttpApi = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    val http    = new Http()
    val userApi = new UserApi(http, currentDb.xa)

    httpApi = new HttpApi(http, userApi.endpoints, TestConfig.api)
  }

  private val stubBackend: SttpBackendStub[Task, Any] = AsyncHttpClientFs2Backend.stub[Task]
  private lazy val serverStub: SttpBackend[Task, Any] = TapirStubInterpreter(stubBackend)
    .whenServerEndpointsRunLogic(httpApi.allEndpoints)
    .backend()

  lazy val requests = new Requests(serverStub)
}
