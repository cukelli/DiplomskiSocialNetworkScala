package enumeration
import scala.util.Try
import play.api.libs.json.{Reads, Writes}


object Genders extends Enumeration {
  type Gender = Value
  val M, F = Value

  implicit val genderReads: Reads[Gender] = Reads.enumNameReads(Genders)
  implicit val genderWrites: Writes[Gender] = Writes.enumNameWrites

  def withNameOpt(s: String): Option[Value] = Try(withName(s)).toOption


}
