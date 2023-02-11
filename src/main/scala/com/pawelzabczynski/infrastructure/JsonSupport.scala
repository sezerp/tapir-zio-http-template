package com.pawelzabczynski.infrastructure

import io.circe.Printer
import io.circe.generic.AutoDerivation

object JsonSupport extends AutoDerivation {
  val noNullsPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)
}
