package controllers

import auth.{AuthAction, UserRequest}
import dao.FriendRequestDao
import enumeration.FriendRequestStatus
import model.FriendRequest
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import requestModels.FriendRequestSend
import service.FriendRequestService
import service.FriendRequestService.{RequestSendFailure, RequestSendSuccess}
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FriendRequestController @Inject()(friendRequestService: FriendRequestService,
                                         cc: ControllerComponents,
                                         authAction: AuthAction,
                                        friendRequestDAO: FriendRequestDao
                                       )(
                                         implicit executionContext: ExecutionContext
                                       ) extends AbstractController(cc) {

  def sendRequest(): Action[JsValue] = authAction.async(parse.json) { implicit request: UserRequest[JsValue] =>
    val senderId = request.userID.getOrElse(throw new IllegalStateException("User not logged in"))
    val friendRequestData = request.body.validate[FriendRequestSend]

    friendRequestData.fold(
      errors => Future.successful(BadRequest(Json.obj("message" -> JsError.toJson(errors)))),
      friendRequest => {
        friendRequestService.sendFriendRequest(senderId, friendRequest.sentFor).map {
          case RequestSendSuccess(message) =>
            Created(Json.obj("message" -> message))
          case RequestSendFailure(message) =>
            BadRequest(Json.obj("message" -> message))
          case _ =>
            Unauthorized(Json.obj("message" -> "Invalid token."))
        }.recover {
          case _: IllegalStateException =>
            Unauthorized(Json.obj("message" -> "You're not logged in."))
          case ex: Exception =>
            BadRequest(Json.obj("message" -> ex.getMessage))
          case _: Throwable =>
            InternalServerError(Json.obj("message" -> "Internal server error"))
        }
      }
    )
  }


  def acceptFriendRequest(userIdSent: Long): Action[AnyContent] = authAction.async
  { implicit request: UserRequest[AnyContent] =>
    val userId = request.userID.getOrElse(throw new IllegalStateException("User ID not found in request"))

    friendRequestDAO.getBySentByAndFor(userIdSent, userId).flatMap {
      case Some(friendRequest) if friendRequest.SentFor == userId && friendRequest.Status ==
        FriendRequestStatus.PENDING =>
        val updatedRequest = friendRequest.copy(Status = FriendRequestStatus.ACCEPTED)
        friendRequestService.updateRequest(updatedRequest)
          .map(_ => Ok(Json.obj("message" -> "Friend request accepted.")))
          .recover {
            case e: Throwable =>
              InternalServerError(Json.obj("message" -> "Internal server error"))
          }
      case _ =>
        Future.successful(BadRequest(Json.obj("message" -> "Invalid friend request or user.")))
    }.recover {
      case _: IllegalStateException =>
        Unauthorized(Json.obj("message" -> "Unauthorized"))
    }
  }



  def declineFriendRequest(userIdSent: Long): Action[AnyContent] = authAction.async
  { implicit request: UserRequest[AnyContent] =>
    val userId = request.userID.getOrElse(throw new IllegalStateException("User ID not found in request"))

    friendRequestDAO.getBySentByAndFor(userIdSent, userId).flatMap {
      case Some(friendRequest) if friendRequest.SentFor == userId && friendRequest.Status ==
        FriendRequestStatus.PENDING =>
         friendRequestService.deleteRequest(friendRequest.RequestID)
          .map(_ => Ok(Json.obj("message" -> "Friend request declined.")))
          .recover {
            case e: Throwable =>
              InternalServerError(Json.obj("message" -> "Internal server error"))
          }
      case _ =>
        Future.successful(BadRequest(Json.obj("message" -> "Invalid request or user.")))
    }.recover {
      case _: IllegalStateException =>
        Unauthorized(Json.obj("message" -> "Unauthorized"))
    }
  }

}
