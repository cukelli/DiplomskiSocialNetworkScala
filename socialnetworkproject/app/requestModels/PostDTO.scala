package requestModels
import java.sql.Timestamp
import play.api.libs.json.{Format, JsError, JsNumber, JsResult, JsSuccess, JsValue, Json, Writes}

object PostDTO {
  implicit object timestampFormat extends Format[Timestamp] {
    override def writes(timestamp: Timestamp): JsValue = JsNumber(timestamp.getTime)
    override def reads(json: JsValue): JsResult[Timestamp] = json match {
      case JsNumber(value) => JsSuccess(new Timestamp(value.toLong))
      case _ => JsError("Error parsing Timestamp")
    }
  }
  implicit val postDTOWrites: Writes[PostDTO] = Json.writes[PostDTO]
  implicit val postDTOReads: Format[PostDTO] = Json.format[PostDTO]

  def tupled: ((Long, Timestamp, String, String,String, String,Int,Boolean)) => PostDTO = {
    case (postID, dateTimeCreated, title, description, firstName, lastName, likes, liked) =>
    PostDTO(postID, dateTimeCreated, title, description, firstName,lastName,likes, liked)
  }

}
case class PostDTO (
                  PostID: Long,
                  DateTImeCreated: Timestamp,
                  Title: String,
                  Description: String,
                  FirstName: String,
                  LastName: String,
                  Likes: Int,
                  HasLiked: Boolean
                )

