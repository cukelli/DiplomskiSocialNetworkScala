package dao
import enumeration.Genders
import enumeration.Roles
import model.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import requestModels.UserSearch
import slick.jdbc.JdbcProfile
import util.Utils
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class UsersTable(tag: Tag) extends Table[User](tag, "Users") {
  def userID = column[Long]("USERID", O.PrimaryKey, O.AutoInc)
  def email = column[String]("EMAIL")
  def lastName = column[String]("LASTNAME")
  def firstName = column[String]("FIRSTNAME")
  def password = column[String]("PASSWORD")
  def gender = column[Genders.Gender]("GENDER")
  def role = column[Roles.Role]("ROLE")
  def profilePicture = column[String]("PROFILEPICTURE")

  def * = (
    userID,
    email,
    lastName,
    firstName,
    password,
    gender,
    role,
    profilePicture
  ) <>
    (User.tupled, User.unapply)

  implicit val genderMapper: BaseColumnType[Genders.Gender] =
    MappedColumnType.base[Genders.Gender, String](
      e => e.toString,
      s => Genders.withName(s)
    )

  implicit val roleMapper: BaseColumnType[Roles.Role] =
    MappedColumnType.base[Roles.Role, String](
      e => e.toString,
      s => Roles.withName(s)
    )
}

class UserDao @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider
)(implicit
    executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val UsersTableQuery = TableQuery[UsersTable]

  val Users = TableQuery[UsersTable]
  def all(): Future[Seq[User]] = db.run(UsersTableQuery.result)
  def getById(userID: Long): Future[Option[User]] =
    db.run(UsersTableQuery.filter(_.userID === userID).result.headOption)

  def getByEmail(email: Option[String]): Future[Option[User]] =
    db.run(UsersTableQuery.filter(_.email === email).result.headOption)
  def getByEmailAndPassword(
      email: String,
      password: String
  ): Future[Option[User]] = {
    db.run(
      Users
        .filter(_.email === email)
        .result
        .map(
          _.headOption.filter(user =>
            Utils.checkPassword(password, user.Password)
          )
        )
    )
  }

  def searchUsers(query: String, loggedUser: Long): Future[Seq[UserSearch]] = {
    val lowercaseQuery = query.toLowerCase
    val queryResult = UsersTableQuery
      .filter(user =>
        (user.userID =!= loggedUser) &&
          ((user.firstName.toLowerCase like s"%$lowercaseQuery%") ||
            (user.lastName.toLowerCase like s"%$lowercaseQuery%"))
      )
    db.run(queryResult.result)
      .map(_.map { user =>
        UserSearch(
          UserID = user.UserID,
          Email = user.Email,
          LastName = user.LastName,
          FirstName = user.FirstName,
          Gender = user.Gender,
          ProfilePicture = user.ProfilePicture
        )
      })
  }

  def create(user: User): Future[Int] = db.run(UsersTableQuery += user)
  def update(user: User): Future[Unit] = db
    .run(UsersTableQuery.filter(_.userID === user.UserID).update(user))
    .map(_ => ())

  def delete(userID: Long): Future[Boolean] =
    db.run(UsersTableQuery.filter(_.userID === userID).delete).map(_ > 0)

  def updateUserImage(userID: Long, imagePath: String): Future[Boolean] = {
    val updateAction = UsersTableQuery
      .filter(_.userID === userID)
      .map(_.profilePicture)
      .update(imagePath)

    db.run(updateAction).map(_ > 0)
  }

  def updatePassword(
      userId: Long,
      oldPassword: String,
      newPassword: String
  ): Future[Boolean] = {
    getById(userId).flatMap {
      case Some(user) if Utils.checkPassword(oldPassword, user.Password) =>
        val hashedNewPassword = Utils.hashPassword(newPassword)
        val updatedUser = user.copy(Password = hashedNewPassword)
        update(updatedUser).map(_ => true)
      case _ => Future.successful(false)
    }
  }

}
