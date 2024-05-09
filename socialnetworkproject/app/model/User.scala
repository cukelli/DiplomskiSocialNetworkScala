package model
import enumeration.Genders
import enumeration.Roles
import play.api.libs.json.{Format, Json, Reads}

object User {
  implicit val userReads: Format[User] = Json.format[User]

  def tupled: (
      (Long, String, String, String, String, Genders.Gender, Roles.Role, String)
  ) => User = {
    case (
          userID,
          email,
          lastName,
          firstName,
          password,
          gender,
          role,
          profilePicture
        ) =>
      User(
        userID,
        email,
        lastName,
        firstName,
        password,
        gender,
        role,
        profilePicture
      )
  }
}

case class User(
    UserID: Long,
    Email: String,
    LastName: String,
    FirstName: String,
    Password: String,
    Gender: Genders.Gender,
    Role: Roles.Role,
    ProfilePicture: String
)
