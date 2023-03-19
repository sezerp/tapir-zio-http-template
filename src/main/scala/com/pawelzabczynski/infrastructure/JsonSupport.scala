package com.pawelzabczynski.infrastructure

import com.pawelzabczynski.security.Role.RoleJsonSupport
import com.pawelzabczynski.util.Id
import com.softwaremill.tagging.@@
import io.circe.{Decoder, Encoder, Printer}
import io.circe.generic.AutoDerivation

import java.util.UUID

object JsonSupport extends AutoDerivation with RoleJsonSupport {
  val noNullsPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)

  implicit def taggedIdEncoder[U]: Encoder[Id @@ U] = Encoder.encodeString.asInstanceOf[Encoder[Id @@ U]]

  implicit def taggedIdDecoder[U]: Decoder[Id @@ U] = Decoder.decodeString.asInstanceOf[Decoder[Id @@ U]]

  implicit def taggedStringEncoder[U]: Encoder[UUID @@ U] = Encoder.encodeString.asInstanceOf[Encoder[UUID @@ U]]

  implicit def taggedStringDecoder[U]: Decoder[UUID @@ U] = Decoder.decodeString.asInstanceOf[Decoder[UUID @@ U]]
}
