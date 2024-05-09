package util
import java.nio.file.{Files, Paths}
import java.util.Base64
import scala.util.Try

object Image {

  def loadAndEncodeImage(imagePath: String): Option[String] = {
    val fullPath = s"public/$imagePath"
    Try(Files.readAllBytes(Paths.get(fullPath))).map { imageBytes =>
      val base64Image = Base64.getEncoder.encodeToString(imageBytes)
      s"data:image/jpeg;base64,$base64Image"
    }.toOption
  }


}
