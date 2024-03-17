package controllers
import enumeration.Roles
import enumeration.Genders
import model.User
import requestModels.UserSearch
import requestModels.{ChangePassword, UserCredentials, UserRegister, UserUpdate}
import service.{FriendRequestService, UserService}
import service.UserService.{
  ChangeImageFailure,
  ChangeImageSuccess,
  ChangePasswordFailure,
  ChangePasswordSuccess,
  UserCreationFailure,
  UserCreationSuccess,
  UserLoggedFailure,
  UserLoggedSuccess,
  UserUpdateFailure,
  UserUpdateSuccess,
  DeleteAccountSuccess,
  DeleteAccountFailure
}
import util.{Utils, Validation}
import auth.{AuthAction, AuthService, UserRequest}
import play.api.libs.Files
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents,
  MultipartFormData
}
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import java.nio.file.{Paths, StandardCopyOption}
import play.api.libs.Files.TemporaryFile

class UserController @Inject() (
    userService: UserService,
    authService: AuthService,
    friendRequestService: FriendRequestService,
    cc: ControllerComponents,
    authAction: AuthAction
) extends AbstractController(cc) {

  def registerUser(): Action[JsValue] = Action.async(parse.json) {
    implicit request =>
      val userData = request.body.validate[UserRegister]

      val result = userData.fold(
        errors =>
          Future.successful(
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          ),
        userRegistrationData => {
          val validationErrors =
            Validation.validateUserRegistration(userRegistrationData)

          if (validationErrors.nonEmpty) {
            val errorMessage = validationErrors.mkString(", ")
            Future.successful(BadRequest(Json.obj("message" -> errorMessage)))
          } else {
            val gender = Genders
              .withNameOpt(userRegistrationData.Gender.toString)
              .getOrElse(Genders.M)
            val hashedPassword =
              Utils.hashPassword(userRegistrationData.Password)
            val profileImage = "images/avatar.jpg"
            val role = Roles
              .withNameOpt(userRegistrationData.Role.toString)
              .getOrElse(Roles.USER)

            val user = User(
              0,
              userRegistrationData.Email,
              userRegistrationData.LastName,
              userRegistrationData.FirstName,
              hashedPassword,
              gender,
              role,
              profileImage
            )
            userService
              .createUser(user)
              .map {
                case UserCreationSuccess(message) =>
                  Created(Json.obj("message" -> message))

                case UserCreationFailure(message) =>
                  BadRequest(Json.obj("message" -> message))
              }
              .recover { case ex: Exception =>
                InternalServerError(Json.obj("message" -> ex.getMessage))
              }
          }
        }
      )

      result
  }

  def getCurrentUser: Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      request.userID match {
        case Some(userId) =>
          userService
            .getUserById(userId)
            .map {
              case Some(user) =>
                val userJson = Json.toJson(user)
                Ok(userJson)
              case None =>
                NotFound(Json.obj("message" -> "User not found"))
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }

        case None =>
          Future.successful(Unauthorized(Json.obj("message" -> "Unauthorized")))
      }
  }

  def login(): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val loginData = request.body.validate[UserCredentials]

    loginData.fold(
      errors =>
        Future.successful(
          BadRequest(Json.obj("message" -> JsError.toJson(errors)))
        ),
      userCredentials => {
        userService
          .getUserByEmailAndPassword(
            userCredentials.Email,
            userCredentials.Password
          )
          .flatMap {
            case UserLoggedSuccess(userId) =>
              val token = authService.encode(userId)
              Future.successful(
                Ok(Json.obj("message" -> "Login successful", "token" -> token))
              )

            case UserLoggedFailure =>
              Future.successful(
                Unauthorized(Json.obj("message" -> "Login failed"))
              )
          }
          .recover { case ex: Exception =>
            InternalServerError(Json.obj("message" -> ex.getMessage))
          }
      }
    )
  }

//  def uploadImage: Action[MultipartFormData[TemporaryFile]] = authAction(parse.multipartFormData).async
//  { implicit request: UserRequest[MultipartFormData[TemporaryFile]] =>
//    val userId = request.userID.getOrElse(throw new IllegalStateException("User ID not found in request"))
//
//    request.body.file("picture").map { picture =>
//      val filename = Paths.get(picture.filename).getFileName
//      val fileSize = picture.fileSize
//      val contentType = picture.contentType
//
//      val imagePath = s"images/$filename"
//      picture.ref.copyTo(Paths.get(s"public/$imagePath"), replace = true)
//
//      userService.updateUserImage(userId, imagePath).flatMap {
//        case ChangeImageSuccess(message) =>
//          Future(Ok(Json.obj("message" -> message)))
//
//        case ChangeImageFailure(message) =>
//          Future(BadRequest(Json.obj("message" -> message)))
//      }
//    }.getOrElse {
//      Future(BadRequest(Json.obj("message" -> "Missing file")))
//    }.recover {
//      case _: IllegalStateException =>
//        Future(Unauthorized(Json.obj("message" -> "You're not logged in.")))
//      case ex: Exception =>
//        Future(InternalServerError(Json.obj("message" -> ex.getMessage)))
//    }
//
//
//  }

  def search(q: String): Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      request.userID match {
        case Some(userId) =>
          userService
            .searchUsers(q, userId)
            .map { users =>
              Ok(Json.toJson(users))
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }
        case None =>
          Future.successful(Unauthorized(Json.obj("message" -> "Unauthorized")))
      }
  }

  def getMyFriends: Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      val userId = request.userID.getOrElse(
        throw new IllegalStateException("User ID not found in request")
      )
      friendRequestService
        .getFriends(userId)
        .map { users =>
          print(users.length);
          Ok(Json.toJson(users))
        }
        .recover { case ex: Exception =>
          InternalServerError(Json.obj("message" -> ex.getMessage))
        }

  }
  def editPersonalInfo: Action[JsValue] = authAction.async(parse.json) {
    implicit request: UserRequest[JsValue] =>
      val updateData = request.body.validate[UserUpdate]

      val result = updateData.fold(
        errors =>
          Future.successful(
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          ),
        userUpdateData => {
          userService
            .updateUser(
              request.userID.getOrElse(
                throw new IllegalStateException("User not logged in")
              ),
              userUpdateData
            )
            .map {
              case UserUpdateSuccess(updatedUser) =>
                Ok(Json.toJson(updatedUser))

              case UserUpdateFailure(message) =>
                if (message == "Unauthorized") {
                  Unauthorized(Json.obj("message" -> "Unauthorized"))
                } else {
                  BadRequest(Json.obj("message" -> message))
                }
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }
        }
      )

      result
  }

  def updatePassword: Action[JsValue] = authAction.async(parse.json) {
    implicit request: UserRequest[JsValue] =>
      val changePasswordData = request.body.validate[ChangePassword]
      val result = changePasswordData.fold(
        errors =>
          Future.successful(
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          ),
        userChangePasswordData => {
          userService
            .updatePassword(
              request.userID.getOrElse(
                throw new IllegalStateException("User not logged in")
              ),
              userChangePasswordData.OldPassword,
              userChangePasswordData.NewPassword
            )
            .map {
              case ChangePasswordSuccess(message) =>
                Ok(Json.obj("message" -> message))

              case ChangePasswordFailure(message) =>
                BadRequest(Json.obj("message" -> message))
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }

        }
      )

      result
  }

  def deleteAccount: Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      request.userID match {
        case Some(userId) =>
          userService
            .deleteUser(userId)
            .map {
              case DeleteAccountSuccess(message) =>
                Ok(Json.obj("message" -> message))
              case DeleteAccountFailure(message) =>
                BadRequest(Json.obj("message" -> message))
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }
        case None =>
          Future.successful(Unauthorized(Json.obj("message" -> "Unauthorized")))
      }
  }

  def getAllUsers: Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      request.userID match {
        case Some(userId) =>
          userService
            .getUserById(userId)
            .flatMap {
              case Some(user) if user.Role == Roles.ADMIN =>
                userService.getAllUsers().map { users =>
                  Ok(Json.toJson(users))
                }
              case Some(_) =>
                Future.successful(
                  Forbidden(
                    Json.obj(
                      "message" -> "Access denied. Only admin can fetch all users."
                    )
                  )
                )
              case None =>
                Future.successful(
                  Unauthorized(Json.obj("message" -> "Unauthorized"))
                )
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }
        case None =>
          Future.successful(Unauthorized(Json.obj("message" -> "Unauthorized")))
      }
  }

  def getAllUsersExceptMe: Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      request.userID match {
        case Some(userId) =>
          userService
            .getUserById(userId)
            .flatMap {
              case Some(user) if user.Role == Roles.ADMIN =>
                for {
                  allUsers <- userService.getAllUsers()
                  friends <- friendRequestService.getFriends(userId)
                  allUserSearch = allUsers.map(user =>
                    UserSearch(
                      UserID = user.UserID,
                      Email = user.Email,
                      LastName = user.LastName,
                      FirstName = user.FirstName,
                      Gender = user.Gender,
                      ProfilePicture = user.ProfilePicture
                    )
                  )
                  usersFiltered = allUserSearch.filterNot(u =>
                    friends.exists(f =>
                      f.UserID == u.UserID
                    ) || u.UserID == userId
                  )
                } yield Ok(Json.toJson(usersFiltered))
              case Some(_) =>
                Future.successful(
                  Forbidden(
                    Json.obj(
                      "message" -> "Access denied."
                    )
                  )
                )
              case None =>
                Future.successful(
                  Unauthorized(Json.obj("message" -> "Unauthorized"))
                )
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }
        case None =>
          Future.successful(Unauthorized(Json.obj("message" -> "Unauthorized")))
      }
  }

}
