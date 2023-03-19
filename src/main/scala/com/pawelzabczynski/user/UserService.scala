package com.pawelzabczynski.user

import com.pawelzabczynski.Fail
import com.pawelzabczynski.account.Account.AccountId
import com.pawelzabczynski.infrastructure.Doobie._
import com.pawelzabczynski.account.{Account, AccountService}
import com.pawelzabczynski.security.AdminRole
import com.pawelzabczynski.security.apiKey.{ApiKey, ApiKeyService}
import com.pawelzabczynski.user.UserService.{
  RegistrationError,
  UserRegistration,
  UserServiceError,
  UserServiceInternalError
}
import com.pawelzabczynski.util.{Clock, ErrorOps, IdGenerator}
import doobie.ConnectionIO
import doobie.util.transactor.Transactor
import zio.{IO, Task, URLayer, ZIO, ZLayer}
import zio.interop.catz._

class UserService(
    accountService: AccountService,
    apiKeyService: ApiKeyService,
    idGenerator: IdGenerator,
    clock: Clock,
    xa: Transactor[Task]
) {
  def register(data: UserRegisterRequest): IO[UserServiceError, UserRegistration] = {
    val transaction = for {
      account <- accountService.crate(data.accountName)
      user    <- createUser(data, account.id)
      _       <- UserModel.insert(user)
      apiKey  <- apiKeyService.create(user.id)
    } yield UserRegistration(user, account, apiKey)

    checkIfUserExist(data.login, data.email) *>
      transaction
        .transact(xa)
        .tapError(e => ZIO.logErrorCause("Error occurred when try register new user", e.toCause))
        .mapError(_ => UserServiceInternalError)
  }

  private def checkIfUserExist(login: String, email: String): IO[UserServiceError, Unit] = {
    for {
      maybeByEmailUser <- UserModel
        .findByEmail(email)
        .transact(xa)
        .tapError(e => ZIO.logErrorCause(s"Error occurred when check if user exist by email", e.toCause))
        .mapError(_ => UserServiceInternalError)
      _ <- ZIO.noneOrFailWith(maybeByEmailUser)(_ => RegistrationError("Email already exists."))
      maybeByLoginUser <- UserModel
        .findByLogin(login)
        .transact(xa)
        .tapError(e => ZIO.logErrorCause("Error occurred when check user exist by login", e.toCause))
        .mapError(_ => UserServiceInternalError)
      _ <- ZIO.noneOrFailWith(maybeByLoginUser)(_ => RegistrationError("Login already exists."))
    } yield ()
  }

  private def createUser(data: UserRegisterRequest, accountId: AccountId): ConnectionIO[User] = {
    for {
      userId <- idGenerator.next[ConnectionIO, User]
      ts     <- clock.now[ConnectionIO]
      hashedPassword = User.hash(data.password)
    } yield User(
      userId,
      accountId,
      data.login,
      AdminRole,
      data.email.toLowerCase,
      data.login.toLowerCase,
      hashedPassword,
      ts
    )
  }
}

object UserService {
  type Env = AccountService with ApiKeyService with IdGenerator with Clock with Transactor[Task]

  case class UserRegistration(user: User, account: Account, apiKey: ApiKey)

  def unsafeCreate(
      accountService: AccountService,
      apiKeyService: ApiKeyService,
      idGenerator: IdGenerator,
      clock: Clock,
      xa: Transactor[Task]
  ): UserService = {
    new UserService(accountService, apiKeyService, idGenerator, clock, xa)
  }

  sealed trait UserServiceError
  case class RegistrationError(msg: String) extends UserServiceError
  case object UserServiceInternalError      extends UserServiceError

  object UserServiceError {
    def toThrowable(ue: UserServiceError): Throwable = {
      ue match {
        case RegistrationError(msg)   => Fail.IncorrectInput(msg)
        case UserServiceInternalError => Fail.InternalServerError
      }
    }
  }

  val live: URLayer[Env, UserService] = ZLayer.fromFunction(unsafeCreate _)
}
