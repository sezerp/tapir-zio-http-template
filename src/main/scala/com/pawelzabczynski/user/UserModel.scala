package com.pawelzabczynski.user

import doobie.ConnectionIO
import com.pawelzabczynski.infrastructure.Doobie._
import java.time.Instant
import cats.syntax.functor._

object UserModel {

  def insert(user: User): ConnectionIO[Unit] = {
    sql"""INSERT INTO users (id, login, email_lowercase, login_lowercase, password, created_on)
         VALUES (${user.id}, ${user.login}, ${user.emailLowercase}, ${user.loginLowercase}, ${user.password}, ${user.createdOn})
       """.stripMargin.update.run.void
  }
}

case class User(
    id: String,
    login: String,
    emailLowercase: String,
    loginLowercase: String,
    password: String,
    createdOn: Instant
)
