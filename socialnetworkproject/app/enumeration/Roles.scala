package enumeration
import scala.util.Try
import play.api.libs.json.{Reads, Writes}


object Roles extends Enumeration {
  type Role = Value
  val USER, ADMIN = Value

  implicit val roleReads: Reads[Role] = Reads.enumNameReads(Roles)
  implicit val roleWrites: Writes[Role] = Writes.enumNameWrites

  def withNameOpt(s: String): Option[Value] = Try(withName(s)).toOption


}
