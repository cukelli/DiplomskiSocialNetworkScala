package auth
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
//import java.time.Clock
import javax.inject.Inject
import scala.util.Try
import play.api.libs.json.Json

class AuthService @Inject()() {
    val key = "secretKey"

  def encode(userID: Long): String = {
    val expirationTime = System.currentTimeMillis / 1000 + 3 * 60 * 60
    val claim = Json.obj(
      "subject" -> userID,
      "exp" -> expirationTime
    )
    val algo = JwtAlgorithm.HS256
    JwtJson.encode(claim, key, algo)
  }

  def decode(token: String): Try[JwtClaim] = {
    JwtJson.decode(token, key, Seq(JwtAlgorithm.HS256))
  }

  def validate(token: String): Try[JwtClaim] = {
    decode(token).map { claims =>
      claims
    }
  }
}
