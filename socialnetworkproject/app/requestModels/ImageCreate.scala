package requestModels
import play.api.libs.json.{Format, Json, Reads}
//post ID is inside URL so no need to send it in request body
case class ImageCreate(
    ImagePath: String,
    PostID: Long
)

object ImageCreate {
  implicit val imageCreateFormat: Format[ImageCreate] = Json.format[ImageCreate]
}
