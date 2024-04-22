package service
import dao.UserDao
import model.User
import play.api.mvc.RequestHeader
import requestModels.{UserSearch, UserUpdate}
import service.UserService.{
  ChangeImageFailure,
  ChangeImageResult,
  ChangeImageSuccess,
  ChangePasswordFailure,
  ChangePasswordResult,
  ChangePasswordSuccess,
  UserCreationFailure,
  UserCreationResult,
  UserCreationSuccess,
  UserLoggedFailure,
  UserLoggedSuccess,
  UserLoginResult,
  UserUpdateFailure,
  UserUpdateResult,
  UserUpdateSuccess,
  DeleteAccountFailure,
  DeleteAccountSuccess,
  DeleteAccountResult
}
import util.Validation

import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import scala.concurrent.Future
class UserService @Inject() (userDAO: UserDao) {

  def getAllUsers(): Future[Seq[User]] = userDAO.all()

  def getUserById(userID: Long): Future[Option[User]] = userDAO.getById(userID)

  def getUserByEmail(email: Option[String]): Future[Option[User]] =
    userDAO.getByEmail(email)

  def searchUsers(query: String, loggedUser: Long): Future[Seq[UserSearch]] = {
    userDAO.searchUsers(query, loggedUser)
  }

  def getUserByEmailAndPassword(
      email: String,
      password: String
  ): Future[UserLoginResult] = {
    userDAO.getByEmailAndPassword(email, password).map {
      case Some(user) => UserLoggedSuccess(user.UserID)
      case None       => UserLoggedFailure
    }
  }

  def createUser(user: User): Future[UserCreationResult] = {
    val result: Future[Int] = userDAO.create(user)
    result.flatMap {
      case 1 =>
        Future.successful(UserCreationSuccess("User created successfully"))
      case 0 => Future.successful(UserCreationFailure("Registration failed"))
    }: Future[UserCreationResult]
  }

  def updateUser(
      userId: Long,
      userUpdateData: UserUpdate
  ): Future[UserUpdateResult] = {
    if (
      !Validation.checkFirstAndLastName(userUpdateData.FirstName) ||
      !Validation.checkFirstAndLastName(userUpdateData.LastName)
    ) {
      Future.successful(
        UserUpdateFailure("First and last name need to have proper format.")
      )
    } else {
      userDAO.getById(userId).flatMap {
        case Some(existingUser) =>
          val updatedUser = existingUser.copy(
            FirstName = userUpdateData.FirstName,
            LastName = userUpdateData.LastName
          )
          userDAO
            .update(updatedUser)
            .map { _ =>
              UserUpdateSuccess(updatedUser)
            }
            .recover { case ex: Exception =>
              UserUpdateFailure("Error while updating user")
            }

        case None =>
          Future.successful(UserUpdateFailure("User not found"))
      }
    }
  }

  def deleteUser(userID: Long): Future[DeleteAccountResult] = {
    userDAO.delete(userID).map {
      case true  => DeleteAccountSuccess("Account deleted successfully")
      case false => DeleteAccountFailure("Account deletion failed")
    }
  }

  def updatePassword(
      userId: Long,
      oldPassword: String,
      newPassword: String
  ): Future[ChangePasswordResult] = {
    if (!Validation.checkPassword(newPassword)) {
      Future.successful(
        ChangePasswordFailure(
          "New password must be in required format: password must have min 8 " +
            "characters, one uppercase letter and one number!"
        )
      )
    } else {
      userDAO.updatePassword(userId, oldPassword, newPassword).flatMap {
        case true =>
          Future
            .successful(ChangePasswordSuccess("Password changed successfully"))
        case false =>
          Future.successful(ChangePasswordFailure("Wrong old password"))
      }: Future[ChangePasswordResult]
    }
  }

  def updateUserImage(
      userId: Long,
      imagePath: String
  ): Future[ChangeImageResult] = {
    userDAO.updateUserImage(userId, imagePath).flatMap {
      case true =>
        Future.successful(ChangeImageSuccess("Password changed successfully"))
      case false => Future.successful(ChangeImageFailure("Wrong old password"))

    }: Future[ChangeImageResult]
  }

}

object UserService {
  sealed trait UserCreationResult
  case class UserCreationSuccess(message: String) extends UserCreationResult
  case class UserCreationFailure(message: String) extends UserCreationResult

  // login messages
  sealed trait UserLoginResult

  case class UserLoggedSuccess(userId: Long) extends UserLoginResult
  case object UserLoggedFailure extends UserLoginResult

  sealed trait UserUpdateResult

  case class UserUpdateSuccess(user: User) extends UserUpdateResult

  case class UserUpdateFailure(message: String) extends UserUpdateResult

  sealed trait ChangePasswordResult
  case class ChangePasswordSuccess(message: String) extends ChangePasswordResult
  case class ChangePasswordFailure(message: String) extends ChangePasswordResult

  sealed trait ChangeImageResult

  case class ChangeImageSuccess(message: String) extends ChangeImageResult

  case class ChangeImageFailure(message: String) extends ChangeImageResult

  sealed trait DeleteAccountResult

  case class DeleteAccountSuccess(message: String) extends DeleteAccountResult

  case class DeleteAccountFailure(message: String) extends DeleteAccountResult

}
