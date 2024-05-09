package requestModels

import play.api.libs.json.{Format, Json}

case class FriendRequestSend(
                       sentFor: Long
                     )

object FriendRequestSend {
  implicit val requestReads: Format[FriendRequestSend] = Json.format[FriendRequestSend]

  def tupled: ((Long)) => FriendRequestSend = {
    case (sentFor) =>
      FriendRequestSend(sentFor)
  }
}
