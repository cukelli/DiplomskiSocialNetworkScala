package requestModels
import enumeration.Genders
import play.api.libs.json.{Format, Json}

object UserSearch {
  implicit val userSearchReads: Format[UserSearch] = Json.format[UserSearch]

  def tupled: ((Long, String, String, String, Genders.Gender, String)) => UserSearch = {
    case (userID, email, lastName, firstName, gender, profilePicture) =>
      UserSearch(userID, email, lastName, firstName, gender, profilePicture)
  }
}


case class UserSearch(
                 UserID: Long,
                 Email: String,
                 LastName: String,
                 FirstName: String,
                 Gender: Genders.Gender,
                 ProfilePicture: String
               )


