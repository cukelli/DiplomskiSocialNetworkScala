package model
import java.sql.Timestamp
import play.api.libs.json.{
  Format,
  JsError,
  JsNumber,
  JsResult,
  JsSuccess,
  JsValue,
  Json,
  Writes
}

object Post {

  implicit object timestampFormat extends Format[Timestamp] {
    override def writes(timestamp: Timestamp): JsValue = JsNumber(
      timestamp.getTime
    )
    override def reads(json: JsValue): JsResult[Timestamp] = json match {
      case JsNumber(value) => JsSuccess(new Timestamp(value.toLong))
      case _               => JsError("Error parsing Timestamp")
    }
  }
  implicit val postReads: Format[Post] = Json.format[Post]
  implicit val postWrites: Writes[Post] = Json.writes[Post]

  def tupled: ((Long, Timestamp, String, String, Long)) => Post = {
    case (
          postID,
          dateTimeCreated,
          title,
          description,
          userPosted
        ) =>
      Post(postID, dateTimeCreated, title, description, userPosted)
  }
}
case class Post(
    PostID: Long,
    DateTImeCreated: Timestamp,
    Title: String,
    Description: String,
    UserPosted: Long
)
