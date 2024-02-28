package model
import enumeration.Genders
import play.api.libs.json.{Format, Json, Reads}

object User {
  implicit val userReads: Format[User] = Json.format[User]

  def tupled: ((Long, String, String, String, String, Genders.Gender, String)) => User = {
    case (userID, email, lastName, firstName, password, gender, profilePicture) =>
      User(userID, email, lastName, firstName, password, gender, profilePicture)
  }
}


case class User(
                 UserID: Long,
                 Email: String,
                 LastName: String,
                 FirstName: String,
                 Password: String,
                 Gender: Genders.Gender,
                 ProfilePicture: String
               )


