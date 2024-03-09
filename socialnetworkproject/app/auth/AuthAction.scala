package auth
// import com.sun.net.httpserver.Authenticator.Result
import play.api.mvc.Result
import pdi.jwt.JwtClaim
import play.api.http.HeaderNames
import play.api.mvc._
import service.UserService
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import play.api.libs.json._


case class UserRequest[A](userID: Option[Long] , request: Request[A])
  extends WrappedRequest[A](request)

class AuthAction @Inject()(bodyParser: BodyParsers.Default, authService: AuthService, userService:
UserService)(implicit ec: ExecutionContext)
  extends ActionBuilder[UserRequest, AnyContent] {

  override def parser: BodyParser[AnyContent] = bodyParser

  override protected def executionContext: ExecutionContext = ec

  private val headerTokenRegex = """Bearer (.+?)""".r

  override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
    extractBearerToken(request) map { token: String =>
      authService.validate(token) match {
        case Success(claim) =>
          val userID: Option[Long] =( Json.parse(claim.content) \ "subject").asOpt[Long]
           val userIDLong = userID.get
          val userCheckDB = userService.getUserById(userIDLong)
          userCheckDB.flatMap{
            case Some(_) =>
              block(UserRequest(userID,request))
            case None =>
              Future.successful(Results.Unauthorized(Json.obj("message" -> "Invalid user")))
          }
        case Failure(t) =>
          Future.successful(Results.Unauthorized(Json.obj("message" -> t.getMessage)))
      }
    } getOrElse(Future.successful(Results.Unauthorized(Json.obj( "message" -> "Invalid token"))))



  private def extractBearerToken[A](request: Request[A]): Option[String] =
    request.headers.get(HeaderNames.AUTHORIZATION) collect {
      case headerTokenRegex(token) => token
    }
}
