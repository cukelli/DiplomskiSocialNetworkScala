package dao
import enumeration.FriendRequestStatus
import model.Post
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import requestModels.PostDTO
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import java.sql.Timestamp
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PostTable(tag: Tag) extends Table[Post](tag, "Post") {

  implicit val statusMapper: BaseColumnType[FriendRequestStatus.Status] =
    MappedColumnType.base[FriendRequestStatus.Status, String](
      e => e.toString,
      s => FriendRequestStatus.withName(s)
    )

  def postID = column[Long]("POSTID", O.PrimaryKey)

  def dateTimeCreated = column[Timestamp]("DATETIMECREATED")

  def title = column[String]("TITLE")

  def description = column[String]("DESCRIPTION")

  def userPosted = column[Long]("USERPOSTED")

  def userPostedFk = foreignKey("user_fk", userPosted, TableQuery[UsersTable])(_.userID)

  def * = (postID, dateTimeCreated, title, description, userPosted) <> (Post.tupled, Post.unapply)
}

class PostDao @Inject() (
                          protected val dbConfigProvider: DatabaseConfigProvider,
                          val friendRequestDao: FriendRequestDao,
                          val userDao: UserDao,
                          val userLikeDao: UserLikeDao
                        )(
                          implicit executionContext: ExecutionContext
                        ) extends HasDatabaseConfigProvider[JdbcProfile] {


  import profile.api._

  val Posts = TableQuery[PostTable]

  def all(): Future[Seq[Post]] = db.run(Posts.result)
  def getById(postID: Long): Future[Option[Post]] = db.run(Posts.filter(_.postID === postID).result.headOption)

  def create(post: Post): Future[Int] = db.run(Posts += post)
  def update(post: Post): Future[Unit] = db.run(Posts.filter(_.postID === post.PostID).update(post)).map(_ => ())
  def delete(postID: Long): Future[Unit] = db.run(Posts.filter(_.postID === postID).delete).map(_ => ())

  def getMyPosts(userId: Long): Future[Seq[Post]] = {
    val userPostsQuery = Posts.filter(_.userPosted === userId)
    db.run(userPostsQuery.result)
  }


  def getFriendsPosts(userId: Long): Future[Seq[PostDTO]] = {
    for {
      friendsIds <- friendRequestDao.getFriendsIds(userId)
      users <- Future.sequence(friendsIds.map(id => userDao.getById(id))).map(_.flatten)
      posts <- Future.sequence(users.map(user => getMyPosts(user.UserID))).map(_.flatten)
      likes <- Future.sequence(posts.map(post => userLikeDao.getByPostId(post.PostID))).map(_.flatten)
    } yield {
      posts.flatMap { post =>
        val user = users.find(u => u.UserID == post.UserPosted)
        user.map { userObject =>
          PostDTO(
            post.PostID,
            post.DateTImeCreated,
            post.Description,
            post.Title,
            userObject.FirstName,
            userObject.LastName,
            likes.count(like => like.postLiked == post.PostID),
            likes.exists(like => (like.userLiked == userId && like.postLiked == post.PostID))

          )
        }
      }
    }
  }

}




