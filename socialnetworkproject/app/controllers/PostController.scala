package controllers
import auth.{AuthAction, UserRequest}
import model.UserLike
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsError, JsValue, Json, Writes}
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents,
  Request
}
import requestModels.{PostCreate, PostDTO}
import service.PostService.{
  PostCreationFailure,
  PostCreationSuccess,
  PostUpdateFailure,
  PostUpdateSuccess
}
import service.UserLikeService.{PostLIkeFailure, PostLikeSuccess}
import service.{PostService, UserLikeService, ImageService}
import service.ImageService.{ImagePostSuccess, ImagePostFailure}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class PostController @Inject() (
    postService: PostService,
    userLikeService: UserLikeService,
    imageService: ImageService,
    cc: ControllerComponents,
    authAction: AuthAction
)(implicit
    executionContext: ExecutionContext
) extends AbstractController(cc) {

  def createPost(): Action[JsValue] = authAction.async(parse.json) {
    implicit request: UserRequest[JsValue] =>
      val postData = request.body.validate[PostCreate]
      val result = postData.fold(
        errors =>
          Future.successful(
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          ),
        postCreationData => {
          postService
            .createPost(
              request.userID.getOrElse(
                throw new IllegalStateException("User not logged in")
              ),
              postCreationData
            )
            .map {
              case PostCreationSuccess(message) =>
                postData.fold(
                  errors =>
                    Future.successful(
                      BadRequest(Json.obj("message" -> JsError.toJson(errors)))
                    ),
                  postCreationData => {
                    imageService
                      .postImage(
                        message,
                        postCreationData.ImagePath
                      )
                      .map {
                        case ImagePostSuccess(message) =>
                          Ok(Json.obj("message" -> message))

                        case ImagePostFailure(message) =>
                          BadRequest(Json.obj("message" -> message))
                      }
                      .recover { case ex: Exception =>
                        InternalServerError(
                          Json.obj("message" -> ex.getMessage)
                        )
                      }
                  }
                )
                Ok(Json.obj("message" -> message))

              case PostCreationFailure(message) =>
                BadRequest(Json.obj("message" -> message))
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }
        }
      )
      print(result);
      result;

  }

  def updatePost(postId: Long): Action[JsValue] = authAction.async(parse.json) {
    implicit request: UserRequest[JsValue] =>
      val updateData = request.body.validate[PostCreate]

      val result = updateData.fold(
        errors =>
          Future.successful(
            BadRequest(Json.obj("message" -> JsError.toJson(errors)))
          ),
        postUpdateData => {
          postService
            .updatePost(
              request.userID.getOrElse(
                throw new IllegalStateException("User not logged in")
              ),
              postId,
              postUpdateData
            )
            .map {
              case PostUpdateSuccess(updatedPost) =>
                Ok(Json.toJson(updatedPost))

              case PostUpdateFailure(message) =>
                BadRequest(Json.obj("message" -> message))
            }
            .recover { case ex: Exception =>
              InternalServerError(Json.obj("message" -> ex.getMessage))
            }
        }
      )

      result
  }

  def likePost(postId: Long): Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      val userId = request.userID.getOrElse(
        throw new IllegalStateException("User ID not found in request")
      )
      val userLike = UserLike(userId, postId)

      userLikeService.hasUserLikedPost(userLike).flatMap { hasLiked =>
        if (hasLiked) {
          Future.successful(
            BadRequest(
              Json.obj("message" -> "You have already liked this post")
            )
          )
        } else {
          userLikeService
            .likePost(userLike)
            .map {
              case PostLikeSuccess(message) =>
                Ok(Json.obj("message" -> message))
              case PostLIkeFailure(message) =>
                BadRequest(Json.obj("message" -> message))
            }
            .recover {
              case _: IllegalStateException =>
                Unauthorized(
                  Json.obj(
                    "message" -> "You can't like a post if you're not logged in."
                  )
                )
              case ex: Exception =>
                InternalServerError(Json.obj("message" -> ex.getMessage))
            }
        }
      }
  }

  def unlikePost(postId: Long): Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      val userId = request.userID.getOrElse(
        throw new IllegalStateException("User ID not found in request")
      )
      userLikeService
        .unlikePost(postId, userId)
        .map { _ =>
          Ok(Json.obj("message" -> "Post unliked"))
        }
        .recover { case ex: Exception =>
          InternalServerError(Json.obj("message" -> ex.getMessage))
        }
  }

  def getPosts: Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      val userId = request.userID.getOrElse(
        throw new IllegalStateException("User ID not found in request")
      )

      postService
        .getFriendsPosts(userId)
        .flatMap { friendsPosts =>
          Future.successful(friendsPosts)
        }
        .map { posts =>
          Ok(Json.toJson(posts))
        }
        .recover {
          case _: IllegalStateException =>
            Unauthorized(Json.obj("message" -> "You are not logged in."))
          case ex: Exception =>
            InternalServerError(Json.obj("message" -> ex.getMessage))
        }
  }

  def getMyPosts: Action[AnyContent] = authAction.async {
    implicit request: UserRequest[AnyContent] =>
      val userId = request.userID.getOrElse(
        throw new IllegalStateException("User ID not found in request")
      )

      postService
        .getMyPosts(userId)
        .map { posts =>
          Ok(Json.toJson(posts))
        }
        .recover {
          case _: IllegalStateException =>
            Unauthorized(Json.obj("message" -> "You are not logged in."))
          case ex: Exception =>
            InternalServerError(Json.obj("message" -> ex.getMessage))
        }
  }
}
