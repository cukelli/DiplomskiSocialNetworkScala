package model
import play.api.libs.json.{Format, Json, Reads}

case class Image(
    PostID: Long,
    ImageID: Long,
    ImagePath: String
)

object Image {
  implicit val imageFormat: Format[Image] = Json.format[Image]

  def tupled: ((Long, Long, String)) => Image = (Image.apply _).tupled
}
