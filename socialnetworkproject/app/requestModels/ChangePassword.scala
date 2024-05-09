package requestModels

import play.api.libs.json.{Format, Json}


object ChangePassword {
  implicit val changePasswordReads: Format[ChangePassword] = Json.format[ChangePassword]

  def tupled: ((String, String)) => ChangePassword = {
    case (oldPassword, newPassword) =>
      ChangePassword(oldPassword, newPassword)
  }
}
case class ChangePassword(
                       OldPassword: String,
                       NewPassword: String,
                     )
