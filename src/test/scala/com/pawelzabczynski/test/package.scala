package com.pawelzabczynski

import com.pawelzabczynski.config.{Config, Sensitive}
import com.pawelzabczynski.http.HttpConfig
import com.pawelzabczynski.infrastructure.DbConfig

package object test {
  val TestConfig: Config = Config(
    HttpConfig("localhost", 8080),
    DbConfig("postgres", Sensitive(""), "", true, "", 1)
  )
}
