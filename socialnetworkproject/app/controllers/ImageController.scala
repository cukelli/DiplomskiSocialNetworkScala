package controllers
import auth.{AuthAction, UserRequest}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsError, JsValue, Json, Writes}
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents,
  Request
}
import service.{PostService, ImageService}
import javax.inject.Inject
import model.Image
import requestModels.ImageCreate
import scala.concurrent.{ExecutionContext, Future}
import service.ImageService.{ImagePostFailure, ImagePostSuccess}

class ImageController @Inject() (
    postService: PostService,
    imageService: ImageService,
    cc: ControllerComponents,
    authAction: AuthAction
)(implicit
    executionContext: ExecutionContext
) extends AbstractController(cc) {

  def createImage(postId: Long, imageID: Long): Action[JsValue] =
    authAction.async(parse.json) { implicit request =>
      request.body
        .validate[(String)]
        .map { case (imagePath) =>
          imageService.postImage(postId, imagePath).map {
            case ImagePostSuccess(message) =>
              Ok(Json.obj("status" -> "success", "message" -> message))
            case ImagePostFailure(message) =>
              BadRequest(Json.obj("status" -> "error", "message" -> message))
          }
        }
        .getOrElse(Future.successful(BadRequest("Invalid request format")))
    }

  def getImagesByPostId(postId: Long): Action[AnyContent] = authAction.async {
    implicit request =>
      imageService.getImagesByPostId(postId).map { images =>
        Ok(Json.toJson(images))
      }
  }

}
