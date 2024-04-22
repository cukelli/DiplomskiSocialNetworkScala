package service
import dao.{FriendRequestDao, UserDao}
import enumeration.FriendRequestStatus
import model.FriendRequest
import model.User
import org.apache.pekko.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import service.FriendRequestService.{
  RequestSendFailure,
  RequestSendSuccess,
  SendRequestResult
}
import requestModels.UserSearch
import java.sql.Timestamp
import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class FriendRequestService @Inject() (
    friendRequestDAO: FriendRequestDao,
    userDao: UserDao
) {

  def getAllRequests(): Future[Seq[FriendRequest]] = friendRequestDAO.all()

  def getRequestByID(requestID: Long): Future[Option[FriendRequest]] =
    friendRequestDAO.getById(requestID)

  def sendFriendRequest(
      sentBy: Long,
      sentFor: Long
  ): Future[SendRequestResult] = {
    if (sentBy == sentFor) {
      Future.successful(
        RequestSendFailure("Cannot send a friend request to yourself.")
      )
    } else {
      for {
        existingRequestOption <- getBySentByAndFor(sentBy, sentFor)
        userSentRequestOption <- getBySentByAndFor(sentFor, sentBy)
        result <- {
          if (existingRequestOption.isEmpty && userSentRequestOption.isEmpty) {
            val currentDateTime: LocalDateTime = LocalDateTime.now()
            val timestamp: Timestamp = Timestamp.valueOf(currentDateTime)

            val newFriendRequest = FriendRequest(
              0,
              timestamp,
              sentBy,
              sentFor,
              FriendRequestStatus.PENDING
            )

            friendRequestDAO.create(newFriendRequest).map {
              case 1 => RequestSendSuccess("Request sent successfully")
              case _ => RequestSendFailure("Error while sending request")
            }
          } else {
            Future.successful(
              RequestSendFailure(
                "Friend request already exists or has been sent."
              )
            )
          }
        }
      } yield result
    }
  }

  def updateRequest(friendRequest: FriendRequest): Future[Unit] =
    friendRequestDAO.update(friendRequest)

  def getFriendsIds(userID: Long): Future[Seq[Long]] =
    friendRequestDAO.getFriendsIds(userID);

  def deleteRequest(requestID: Long): Future[Unit] =
    friendRequestDAO.delete(requestID)

  def getBySentByAndFor(
      sentBy: Long,
      sentFor: Long
  ): Future[Option[FriendRequest]] =
    friendRequestDAO.getBySentByAndFor(sentBy, sentFor)

  def getFriends(sentBy: Long): Future[Seq[UserSearch]] = {
    friendRequestDAO.getFriendsIds(sentBy).flatMap { friendIds =>
      val futures: Seq[Future[Option[UserSearch]]] = friendIds.map { id =>
        userDao.getById(id).map { maybeUser =>
          maybeUser.map { user =>
            UserSearch(
              UserID = user.UserID,
              Email = user.Email,
              LastName = user.LastName,
              FirstName = user.FirstName,
              Gender = user.Gender,
              ProfilePicture = user.ProfilePicture
            )
          }
        }
      }
      Future.sequence(futures).map { seqOptionUserSearch =>
        seqOptionUserSearch.flatten
      }
    }
  }

}

object FriendRequestService {
  sealed trait SendRequestResult
  case class RequestSendSuccess(message: String) extends SendRequestResult
  case class RequestSendFailure(message: String) extends SendRequestResult

}
