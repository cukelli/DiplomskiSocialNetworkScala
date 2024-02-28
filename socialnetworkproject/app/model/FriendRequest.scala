package model

import enumeration.FriendRequestStatus
import play.api.libs.json.{Format, JsError, JsNumber, JsResult, JsSuccess, JsValue, Json}

import java.sql.Timestamp


object FriendRequest {

  implicit object timestampFormat extends Format[Timestamp] {
    override def writes(timestamp: Timestamp): JsValue = JsNumber(timestamp.getTime)

    override def reads(json: JsValue): JsResult[Timestamp] = json match {
      case JsNumber(value) => JsSuccess(new Timestamp(value.toLong))
      case _ => JsError("Error parsing Timestamp")
    }
  }



  implicit val friendRequestReads: Format[FriendRequest] = Json.format[FriendRequest]

  def tupled: ((Long, Timestamp, Long, Long, FriendRequestStatus.Status)) => FriendRequest = {
    case (requestID, createdAt, sentBy, sentFor, status) =>
      FriendRequest(requestID, createdAt, sentBy, sentFor, status)
  }
}
case class FriendRequest(RequestID: Long,
                         CreatedAt: Timestamp,
                         SentBy: Long,
                         SentFor: Long,
                         Status: FriendRequestStatus.Value)
