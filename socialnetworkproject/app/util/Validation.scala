package util

import requestModels.UserRegister

object Validation {

  //valid emails: korisnik@example.com
  //    ime.prezime@domen.com
  //    info@my-domain.net

  //invalid emails: korisnik@com
  //    @example.com (
  //    ime&prezime@example.com
  private val emailRegex = {
  """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r


  }

  //matches typical names like 'John Doe'
  private val firstLastNameRegex = {
   """^[a-zA-Z\- ]+$""".r

  }

  //password must have min 8 characters, one uppercase letter and one number
  private val passwordRegex = {
    """^(?=.*[A-Z])(?=.*\d).{8,}$""".r
  }


  private def validate(input: String, regex: scala.util.matching.Regex): Boolean =
    Option(input).exists(_.trim.nonEmpty) && regex.findFirstMatchIn(input).isDefined

  def checkEmail(e: String): Boolean = validate(e, emailRegex)

  def checkFirstAndLastName(e: String): Boolean = validate(e, firstLastNameRegex)

  def checkPassword(e: String): Boolean = validate(e, passwordRegex)

   def validateUserRegistration(userData: UserRegister): Seq[String] = {
    Seq(
      if (!Validation.checkEmail(userData.Email)) Some("Invalid email") else None,
      if (!Validation.checkFirstAndLastName(userData.FirstName)) Some("Invalid first name") else None,
      if (!Validation.checkFirstAndLastName(userData.LastName)) Some("Invalid last name") else None,
      if (!Validation.checkPassword(userData.Password)) Some("Invalid password") else None
    ).flatten
  }



}
