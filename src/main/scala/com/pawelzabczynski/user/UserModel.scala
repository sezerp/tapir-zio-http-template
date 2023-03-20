package com.pawelzabczynski.user

import doobie.ConnectionIO
import com.pawelzabczynski.infrastructure.Doobie._

import java.time.Instant
import cats.syntax.functor._
import com.pawelzabczynski.account.Account.AccountId
import com.pawelzabczynski.security.Role
import com.pawelzabczynski.user.User.UserId
import com.pawelzabczynski.util.Id
import com.softwaremill.tagging.@@

object UserModel {
  def insert(user: User): ConnectionIO[Unit] = {
    sql"""INSERT INTO users (id, account_id, login, "role", email_lowercase, login_lowercase, password, created_on)
         VALUES (${user.id}, ${user.accountId}, ${user.login}, ${user.role}, ${user.emailLowercase}, ${user.loginLowercase}, ${user.password}, ${user.createdAt})
       """.stripMargin.update.run.void
  }

  def login(emailOrLogin: String): ConnectionIO[Option[User]] = {
    val emailOrLoginLowercase = emailOrLogin.toLowerCase
    findBy(fr"WHERE email_lowercase = $emailOrLoginLowercase OR login_lowercase = $emailOrLoginLowercase")
  }

  def findById(id: UserId): ConnectionIO[Option[User]] = {
    findBy(fr"WHERE id = $id")
  }

  def findByEmail(email: String): ConnectionIO[Option[User]] = {
    val emailLowercase = email.toLowerCase
    findBy(fr"WHERE email_lowercase = $emailLowercase")
  }

  def findByLogin(login: String): ConnectionIO[Option[User]] = {
    val loginLoweCase = login.toLowerCase
    findBy(fr"WHERE login_lowercase = $loginLoweCase")
  }

  private def findBy(where: Fragment): ConnectionIO[Option[User]] = {
    (fr"""
        SELECT id, account_id, login, "role", email_lowercase, login_lowercase, password, created_on
        FROM users""".stripMargin ++ where)
      .query[User]
      .option
  }
}

case class User(
    id: UserId,
    accountId: AccountId,
    login: String,
    role: Role,
    emailLowercase: String,
    loginLowercase: String,
    password: String,
    createdAt: Instant
)

object User {
  import com.github.t3hnar.bcrypt._
  type UserId = Id @@ User

  def hash(password: String): String = {
    password.bcryptBounded(11)
  }

  def verifyPw(pw: String, pwHash: String): Boolean = {
    pw.isBcryptedBounded(pwHash)
  }
}
