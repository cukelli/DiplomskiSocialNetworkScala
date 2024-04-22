package dao
import model.Image
import dao.PostDao
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.jdbc.MySQLProfile.api._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ImageTable(tag: Tag) extends Table[Image](tag, "Image") {

  def postID = column[Long]("POSTID", O.PrimaryKey)

  def imageID = column[Long]("IMAGEID", O.PrimaryKey)

  def imagePath = column[String]("IMAGEPATH")

  def * = (
    postID,
    imageID,
    imagePath
  ) <> (Image.tupled, Image.unapply)
}

class ImageDao @Inject() (
    protected val dbConfigProvider: DatabaseConfigProvider,
    val postDao: PostDao
)(implicit
    executionContext: ExecutionContext
) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  private val ImageTableQuery = TableQuery[ImageTable]
  val Images = TableQuery[ImageTable]

  def create(image: Image): Future[Int] = db.run(ImageTableQuery += image)

  def findImagesByPostId(postId: Long): Future[Seq[Image]] = {
    db.run(Images.filter(_.postID === postId).result)
  }

}
