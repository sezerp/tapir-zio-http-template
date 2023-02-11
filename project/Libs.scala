import sbt._
import Versions._

object Libs {

  lazy val coreDeps = Seq(
    "dev.zio" %% "zio" % zioVersion
  )

  lazy val webDeps = Seq(
    "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server-zio"       % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-http4s-server"           % tapirVersion,
    "org.http4s"                    %% "http4s-blaze-server"           % "0.23.13",
    "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics"      % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-swagger-ui-bundle"       % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-json-circe"              % tapirVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"        % tapirVersion % Test,
    "com.softwaremill.sttp.client3" %% "async-http-client-backend-fs2" % sttpVersion,
    "com.softwaremill.sttp.client3" %% "slf4j-backend"                 % sttpVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-sttp-stub-server"        % tapirVersion
  )

  lazy val loggingDeps = Seq(
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.2",
    "ch.qos.logback"              % "logback-classic" % "1.2.3",
    "org.codehaus.janino"         % "janino"          % "3.1.0",
    "de.siegmar"                  % "logback-gelf"    % "2.2.0",
    "dev.zio"                    %% "zio-logging"     % "2.1.8"
  )

  lazy val testDeps = Seq(
    "dev.zio"                       %% "zio-test"     % "2.0.5"  % Test,
    "dev.zio"                       %% "zio-test-sbt" % "2.0.5"  % Test,
    "com.softwaremill.sttp.client3" %% "circe"        % "3.8.8"  % Test,
    "org.scalatest"                 %% "scalatest"    % "3.1.1"  % Test,
    "com.softwaremill.quicklens"    %% "quicklens"    % "1.4.12" % Test
  )

  val configDeps = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.17.1"
  )

  val monitoringDeps = Seq(
    "io.prometheus"                  % "simpleclient"             % prometheusVersion,
    "io.prometheus"                  % "simpleclient_hotspot"     % prometheusVersion,
    "com.softwaremill.sttp.client3" %% "prometheus-backend"       % sttpVersion,
    "com.softwaremill.sttp.tapir"   %% "tapir-prometheus-metrics" % tapirVersion
  )

  lazy val allDeps = coreDeps ++ webDeps ++ loggingDeps ++ testDeps ++ configDeps ++ monitoringDeps
}

private object Versions {
  val zioVersion          = "2.0.6"
  val tapirVersion        = "1.2.5"
  val prometheusVersion   = "0.15.0"
  val sttpVersion         = "3.6.2"
}
