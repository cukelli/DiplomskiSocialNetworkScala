package service
import scala.concurrent.ExecutionContext.Implicits.global
import dao.UserLikeDao
import model.UserLike
import play.api.mvc.RequestHeader
import javax.inject.Inject
import scala.concurrent.Future
import service.UserLikeService.{LikeResult, PostLikeSuccess, PostLIkeFailure}


class UserLikeService @Inject()(userLikeDAO: UserLikeDao) {

  def likePost(userLike: UserLike): Future[LikeResult] = {
    val result: Future[Int] = userLikeDAO.likePost(userLike)
    result.flatMap {
      case 1 => Future.successful(PostLikeSuccess("You liked a post"))
      case 0 => Future.successful(PostLIkeFailure("Post unliked"))
    }: Future[LikeResult]
  }
  def hasUserLikedPost(userLike: UserLike): Future[Boolean] = {
    userLikeDAO.hasUserLikedPost(userLike)
  }
  def unlikePost(postID: Long, userID: Long): Future[Unit] = userLikeDAO.unlike(postID, userID)

  def getByPostId(postId: Long): Future[Seq[UserLike]] = {
    userLikeDAO.getByPostId(postId);
  }

  def getAllPosts(): Future[Seq[UserLike]] = userLikeDAO.all()

}
  object UserLikeService {
    sealed trait LikeResult
    case class PostLikeSuccess(message: String) extends LikeResult

    case class PostLIkeFailure(message: String) extends LikeResult


  }



