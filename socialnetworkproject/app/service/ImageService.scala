package service
import model.Image
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.Inject
import scala.concurrent.Future
import dao.ImageDao
import service.ImageService.{
  ImagePostResult,
  ImagePostFailure,
  ImagePostSuccess
}

class ImageService @Inject() (imageDao: ImageDao) {
  def postImage(postId: Long, imagePath: String): Future[ImagePostResult] = {
    val image = Image(postId, 0, imagePath)
    imageDao.create(image).map {
      case 1 => ImagePostSuccess("Image posted successfully")
      case _ => ImagePostFailure("Image posting failed")
    }
  }

  def getImagesByPostId(postId: Long): Future[Seq[Image]] = {
    imageDao.findImagesByPostId(postId)
  }
}

object ImageService {
  sealed trait ImagePostResult
  case class ImagePostSuccess(message: String) extends ImagePostResult
  case class ImagePostFailure(message: String) extends ImagePostResult

}
