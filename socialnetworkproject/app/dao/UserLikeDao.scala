package dao
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import model.UserLike
import slick.jdbc.MySQLProfile.api._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


class UserLikeTable(tag: Tag) extends Table[UserLike](tag, "UserLike") {

  def userLiked = column[Long]("USERLIKED")

  def postLiked = column[Long]("POSTLIKED")

  def userLikedFk = foreignKey("userLiked_fk", userLiked, TableQuery[UsersTable])(_.userID)

  def postLikedFk = foreignKey("postLiked_fk", postLiked, TableQuery[PostTable])(_.postID)

  def pk = primaryKey("pk_user_like", (userLiked, postLiked))

  def * = (userLiked, postLiked) <> (UserLike.tupled, UserLike.unapply)
}

class UserLikeDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider
                           )(
                             implicit executionContext: ExecutionContext
                           ) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val UserLikesTableQuery = TableQuery[UserLikeTable]

  def all(): Future[Seq[UserLike]] = db.run(UserLikesTableQuery.result)

  def likePost(userLike: UserLike): Future[Int] = db.run(UserLikesTableQuery += userLike)

  def hasUserLikedPost(userLike: UserLike): Future[Boolean] = {
    db.run(UserLikesTableQuery.filter(like =>
      like.userLiked === userLike.userLiked &&
        like.postLiked === userLike.postLiked
    ).exists.result)
  }

  def getByPostId(postId: Long): Future[Seq[UserLike]] = {
    db.run(UserLikesTableQuery.filter(_.postLiked === postId).result)
  }

  def unlike(userID: Long, postID: Long): Future[Unit] = {
    db.run(UserLikesTableQuery.filter(like => like.userLiked === userID && like.postLiked === postID).delete).map(_ => ())
  }

}

