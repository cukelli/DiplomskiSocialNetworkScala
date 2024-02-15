package enumeration

import play.api.libs.json.{Reads, Writes}

import scala.util.Try

object FriendRequestStatus extends Enumeration {
  type Status = Value
  val PENDING, ACCEPTED, DECLINED = Value

  implicit val friendRequestStatusReads: Reads[Status] = Reads.enumNameReads(FriendRequestStatus)
  implicit val friendRequestStatusWrites: Writes[Status] = Writes.enumNameWrites

  def withNameOpt(s: String): Option[Value] = Try(withName(s)).toOption


}
