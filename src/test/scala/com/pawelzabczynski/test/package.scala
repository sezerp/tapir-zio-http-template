package com.pawelzabczynski

import com.pawelzabczynski.config.Config
import com.pawelzabczynski.http.HttpConfig

package object test {
  val TestConfig: Config = Config(
    HttpConfig("localhost", 8080)
  )
}
