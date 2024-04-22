package service
import dao.{PostDao, ImageDao}
import model.Post
import play.api.mvc.RequestHeader
import requestModels.{PostCreate, PostDTO}
import scala.concurrent.ExecutionContext.Implicits.global
import service.PostService.{
  PostCreationFailure,
  PostCreationResult,
  PostCreationSuccess,
  PostUpdateFailure,
  PostUpdateResult,
  PostUpdateSuccess
}
import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.Future

class PostService @Inject() (postDAO: PostDao, imageDao: ImageDao) {

  def getAllPosts: Future[Seq[Post]] = postDAO.all()

  def getPostById(postID: Long): Future[Option[Post]] = postDAO.getById(postID)

  def getFriendsPosts(userID: Long): Future[Seq[PostDTO]] =
    postDAO.getFriendsAndMyPosts(userID);

  def getMyPosts(userId: Long): Future[Seq[Post]] = postDAO.getMyPosts(userId)

  def createPost(
      userId: Long,
      postCreationData: PostCreate
  ): Future[PostCreationResult] = {
    if (
      postCreationData.Title.trim.isEmpty || postCreationData.Description.trim.isEmpty
    ) {
      Future.successful(
        PostCreationFailure("Title and description cannot be empty")
      )
    } else {
      val currentDateTime: LocalDateTime = LocalDateTime.now()
      val timestamp: Timestamp = Timestamp.valueOf(currentDateTime)

      val post = Post(
        0,
        timestamp,
        postCreationData.Title,
        postCreationData.Description,
        userId
      )

      val result: Future[Long] = postDAO.create(post)
      result.map { postId =>
        if (postId != 0) {
          PostCreationSuccess(postId)
        } else {
          PostCreationFailure("Post creation failed")
        }
      }

    }
  }

  def updatePost(
      userId: Long,
      postId: Long,
      postUpdateData: PostCreate
  ): Future[PostUpdateResult] = {
    if (
      postUpdateData.Title.trim.isEmpty || postUpdateData.Description.trim.isEmpty
    ) {
      Future.successful(
        PostUpdateFailure("Title and description cannot be empty")
      )
    } else {
      val currentDateTime: LocalDateTime = LocalDateTime.now()
      val timestamp: Timestamp = Timestamp.valueOf(currentDateTime)

      postDAO.getById(postId).flatMap {
        case Some(existingPost) if existingPost.UserPosted == userId =>
          val updatedPost = existingPost.copy(
            postId,
            timestamp,
            postUpdateData.Title,
            postUpdateData.Description,
            userId
          )

          postDAO
            .update(updatedPost)
            .map { _ =>
              PostUpdateSuccess(updatedPost)
            }
            .recover { case ex: Exception =>
              PostUpdateFailure("Error while updating post")
            }

        case Some(_) =>
          Future.successful(
            PostUpdateFailure("You are not the creator of this post")
          )

        case None =>
          Future.successful(PostUpdateFailure("Post not found"))
      }
    }
  }
  def deletePost(postID: Long): Future[Unit] = postDAO.delete(postID)

}

object PostService {
  sealed trait PostCreationResult
  case class PostCreationSuccess(message: Long) extends PostCreationResult
  case class PostCreationFailure(message: String) extends PostCreationResult

  sealed trait PostUpdateResult

  case class PostUpdateSuccess(post: Post) extends PostUpdateResult

  case class PostUpdateFailure(message: String) extends PostUpdateResult

}
