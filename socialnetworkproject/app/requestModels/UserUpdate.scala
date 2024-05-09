package requestModels

import play.api.libs.json.{Format, Json}


object UserUpdate {
  implicit val userUpdateReads: Format[UserUpdate] = Json.format[UserUpdate]

  def tupled: ((String, String)) => UserUpdate = {
    case (firstName, lastName) =>
      UserUpdate(firstName, lastName)
  }
}
case class UserUpdate(
                       FirstName: String,
                       LastName: String,
                     )

