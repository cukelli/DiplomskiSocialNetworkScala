package requestModels

import enumeration.Genders
import play.api.libs.json.{Format, Json, Reads}

object PostCreate {
  implicit val postReads: Format[PostCreate] = Json.format[PostCreate]

  def tupled: ((String, String, String)) => PostCreate = {
    case (title, description, imagePaths) =>
      PostCreate(title, description, imagePaths)
  }
}

case class PostCreate(
    Title: String,
    Description: String,
    ImagePath: String
)
