package model
import play.api.libs.json.{Format, Json}

object UserLike {
  implicit val userLikeReads: Format[UserLike] = Json.format[UserLike]

  def tupled: (( Long, Long)) => UserLike = {
    case (userLiked, postLIked) =>
      UserLike(userLiked, postLIked)
  }
}


case class UserLike(
                     userLiked: Long,
                     postLiked: Long
               )
