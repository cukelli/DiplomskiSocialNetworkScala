package requestModels
import enumeration.Genders
import enumeration.Roles
import play.api.libs.json.{Format, Json, Reads}

object UserRegister {
  implicit val userReads: Format[UserRegister] = Json.format[UserRegister]

  def tupled: ((String, String, String, String, Genders.Gender, Roles.Role)) => UserRegister = {
    case (email, lastName, firstName, password, gender, role) =>
      UserRegister(email, lastName, firstName, password, gender, role)
  }
}

case class UserRegister(
                 Email: String,
                 LastName: String,
                 FirstName: String,
                 Password: String,
                 Gender: Genders.Gender,
                 Role:  Roles.Role
                       )
