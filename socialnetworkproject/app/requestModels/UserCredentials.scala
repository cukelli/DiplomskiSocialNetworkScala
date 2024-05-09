package requestModels

import play.api.libs.json.{Format, Json}

object UserCredentials {
  implicit val userReads: Format[UserCredentials] = Json.format[UserCredentials]

}

case class UserCredentials(
                         Email: String,
                         Password: String

                       )
